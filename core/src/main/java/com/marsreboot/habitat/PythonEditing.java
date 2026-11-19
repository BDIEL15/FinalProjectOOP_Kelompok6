package com.marsreboot.habitat;

import java.util.ArrayList;
import java.util.List;

/** Local text transformations, not a Python parser or an execution validator. */
final class PythonEditing {
    record Edit(String text, int anchor, int cursor) { }
    private record Change(int at, int remove, String insert) { }

    static Edit edit(String text, int anchor, int cursor, char key, boolean shift) {
        int lo=Math.min(anchor,cursor), hi=Math.max(anchor,cursor);
        int lineStart=text.lastIndexOf('\n',lo-1)+1;
        Context context=scan(text,lo);
        if(key=='\t') {
            if(lo!=hi || shift) return indent(text,anchor,cursor,shift);
            int column=0;
            for(int i=lineStart;i<lo;i++) column+=text.charAt(i)=='\t'?4-column%4:1;
            return replace(text,lo,hi," ".repeat(4-column%4));
        }
        if(key=='\n' || key=='\r') {
            String base=leading(text,lineStart,lo);
            if(context.quote!=0 && context.triple) return replace(text,lo,hi,"\n");
            boolean deeper=context.quote==0 && (!scan(text.substring(lineStart,lo),lo-lineStart).brackets.isEmpty() || context.last==':');
            String indentation=base+(deeper?"    ":"");
            if(deeper && lo==hi && hi<text.length() && matches(context.top(),text.charAt(hi))) {
                String insert="\n"+indentation+"\n"+base;
                return new Edit(text.substring(0,lo)+insert+text.substring(hi),lo+1+indentation.length(),lo+1+indentation.length());
            }
            return replace(text,lo,hi,"\n"+indentation);
        }
        if(key=='\b' && lo==hi && lo>0) {
            char previous=text.charAt(lo-1);
            if(!context.comment && hi<text.length() && matches(previous,text.charAt(hi))
                && !escaped(text,lo-1)) return replace(text,lo-1,hi+1,"");
            String prefix=text.substring(lineStart,lo);
            if(!prefix.isEmpty() && prefix.chars().allMatch(c->c==' ')) {
                int count=(prefix.length()-1)%4+1;
                return replace(text,lo-count,hi,"");
            }
        }
        if(context.comment || context.triple) return null;
        char next=hi<text.length()?text.charAt(hi):0;
        if(lo==hi && next==key && !escaped(text,lo)
            && ((context.quote==key && (key=='\'' || key=='"')) || (context.quote==0 && matches(context.top(),key)))) {
            return new Edit(text,lo+1,lo+1);
        }
        if(context.quote!=0) return null;
        char closing=switch(key) {case '('->')';case '['->']';case '{'->'}';case '\'', '"'->key;default->0;};
        if(closing!=0 && (lo!=hi || next==0 || Character.isWhitespace(next) || ")]},:;".indexOf(next)>=0)) {
            String selected=text.substring(lo,hi);
            String result=text.substring(0,lo)+key+selected+closing+text.substring(hi);
            return new Edit(result,lo+1,lo+1+selected.length());
        }
        return null; // Native TextArea handles typing, Delete, clipboard, navigation and Undo.
    }
    private static Edit replace(String text,int start,int end,String insertion) {
        int caret=start+insertion.length();
        return new Edit(text.substring(0,start)+insertion+text.substring(end),caret,caret);
    }
    private static Edit indent(String text,int anchor,int cursor,boolean backwards) {
        int lo=Math.min(anchor,cursor),hi=Math.max(anchor,cursor);
        int first=text.lastIndexOf('\n',lo-1)+1;
        int last;
        // End at a new line's beginning means that line is not selected.
        if(lo==hi) last=first;
        else last=text.lastIndexOf('\n',hi-2)+1;
        List<Change> changes=new ArrayList<>();
        for(int at=first;at<=last;) {
            int remove=0;
            if(backwards) {
                while(remove<4 && at+remove<text.length() && text.charAt(at+remove)==' ') remove++;
                if(remove==0 && at<text.length() && text.charAt(at)=='\t') remove=1;
            }
            changes.add(new Change(at,remove,backwards?"":"    "));
            int next=text.indexOf('\n',at); if(next<0)break; at=next+1;
        }
        StringBuilder result=new StringBuilder(text);
        for(int i=changes.size()-1;i>=0;i--) {
            Change c=changes.get(i);result.replace(c.at,c.at+c.remove,c.insert);
        }
        return new Edit(result.toString(),map(anchor,changes),map(cursor,changes));
    }
    private static int map(int position,List<Change> changes) {
        int delta=0;
        for(Change c:changes) {
            if(position<c.at)break;
            if(position<c.at+c.remove)return c.at+delta;
            delta+=c.insert.length()-c.remove;
        }
        return position+delta;
    }
    private static String leading(String text,int start,int end) {
        int i=start;while(i<end && (text.charAt(i)==' ' || text.charAt(i)=='\t'))i++;
        return text.substring(start,i);
    }
    private static boolean escaped(String text,int at) {
        int slashes=0;for(int i=at-1;i>=0 && text.charAt(i)=='\\';i--)slashes++;
        return slashes%2==1;
    }
    private static boolean matches(char open,char close) {
        return open=='(' && close==')' || open=='[' && close==']' || open=='{' && close=='}'
            || (open=='\'' || open=='"') && open==close;
    }
    private static final class Context {
        char quote,last;
        boolean triple,comment;
        StringBuilder brackets=new StringBuilder();
        char top(){return brackets.isEmpty()?0:brackets.charAt(brackets.length()-1);}
    }
    private static Context scan(String text,int end) {
        Context c=new Context();
        for(int i=0;i<end;i++) {
            char ch=text.charAt(i);
            if(ch=='\n') {c.comment=false;c.last=0;}
            if(c.comment)continue;
            if(c.quote!=0) {
                if(ch==c.quote && !escaped(text,i)) {
                    if(!c.triple) {c.quote=0;c.last='s';}
                    else if(i+2<end && text.charAt(i+1)==ch && text.charAt(i+2)==ch) {c.quote=0;c.triple=false;i+=2;c.last='s';}
                }
                continue;
            }
            if(ch=='#') {c.comment=true;continue;}
            if(ch=='\'' || ch=='"') {
                c.quote=ch;c.triple=i+2<end && text.charAt(i+1)==ch && text.charAt(i+2)==ch;
                if(c.triple)i+=2;
                continue;
            }
            if("([{ ".indexOf(ch)>=0 && ch!=' ')c.brackets.append(ch);
            else if(matches(c.top(),ch))c.brackets.setLength(c.brackets.length()-1);
            if(!Character.isWhitespace(ch))c.last=ch;
        }
        return c;
    }
}
