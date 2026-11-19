package com.marsreboot.habitat;

import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.InputListener;
import com.badlogic.gdx.scenes.scene2d.ui.TextArea;
import com.badlogic.gdx.scenes.scene2d.utils.UIUtils;

/** Small editing assistance on the existing Scene2D widget; native Undo and clipboard are retained. */
final class PythonCodeArea extends TextArea {
    PythonCodeArea(String text,TextFieldStyle style) {
        super(text,style);
        setFocusTraversal(false);
        setOnlyFontChars(false);
    }
    @Override protected InputListener createInputListener() {
        return new TextAreaListener() {
            @Override public boolean keyTyped(InputEvent event,char character) {
                if(isDisabled() || !hasKeyboardFocus())return super.keyTyped(event,character);
                var edit=PythonEditing.edit(text,hasSelection?selectionStart:cursor,cursor,character,UIUtils.shift());
                if(edit==null)return super.keyTyped(event,character);
                if(!edit.text().equals(text)) {
                    int lo=Math.min(hasSelection?selectionStart:cursor,cursor);
                    int hi=Math.max(hasSelection?selectionStart:cursor,cursor);
                    if(character=='\t') {
                        // Indent only each line's whitespace; do not retype the unchanged block.
                        String[] oldLines=text.split("\n",-1),newLines=edit.text().split("\n",-1);
                        int at=text.length();
                        for(int line=oldLines.length-1;line>=0;line--) {
                            at-=oldLines[line].length();
                            if(!oldLines[line].equals(newLines[line])) replaceNative(event,at,oldLines[line],newLines[line]);
                            at--;
                        }
                    } else if(lo!=hi && "([{\"'".indexOf(character)>=0) {
                        replaceNative(event,hi,"",String.valueOf(edit.text().charAt(hi+1)));
                        replaceNative(event,lo,"",String.valueOf(character));
                    } else replaceNative(event,0,text,edit.text());
                }
                // setSelection also updates TextArea's cursor line/scroll state.
                PythonCodeArea.this.setCursorPosition(edit.cursor());
                setSelection(edit.cursor(),edit.cursor());
                selectionStart=edit.anchor();hasSelection=edit.anchor()!=edit.cursor();
                // Native TextArea refreshes scrolling even for a non-printable no-op.
                super.keyTyped(event, '\0');
                return true;
            }
            private void replaceNative(InputEvent event,int offset,String before,String after) {
                int start=0,oldEnd=before.length(),newEnd=after.length();
                while(start<oldEnd && start<newEnd && before.charAt(start)==after.charAt(start))start++;
                while(oldEnd>start && newEnd>start && before.charAt(oldEnd-1)==after.charAt(newEnd-1)){oldEnd--;newEnd--;}
                PythonCodeArea.this.setCursorPosition(offset+start);
                setSelection(offset+start,offset+oldEnd);
                String insertion=after.substring(start,newEnd);
                if(insertion.isEmpty())super.keyTyped(event,'\b');
                else for(int i=0;i<insertion.length();i++)super.keyTyped(event,insertion.charAt(i));
            }
        };
    }
}
