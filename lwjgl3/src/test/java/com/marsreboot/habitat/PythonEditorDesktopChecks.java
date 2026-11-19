package com.marsreboot.habitat;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.Input.Keys;
import com.badlogic.gdx.InputProcessor;
import com.badlogic.gdx.scenes.scene2d.ui.TextArea;
import java.lang.reflect.Proxy;

/** Sends events through the real game's InputMultiplexer/Stage, not directly to the editing model. */
final class PythonEditorDesktopChecks {
    static int run(TextArea editor,InputProcessor processor) {
        Input original=Gdx.input;
        boolean[] modifiers=new boolean[2];
        Gdx.input=(Input)Proxy.newProxyInstance(Input.class.getClassLoader(),new Class<?>[]{Input.class},(proxy,method,args)->{
            if(method.getName().equals("isKeyPressed") && args!=null) {
                int key=(Integer)args[0];
                if(key==Keys.CONTROL_LEFT || key==Keys.CONTROL_RIGHT)return modifiers[0];
                if(key==Keys.SHIFT_LEFT || key==Keys.SHIFT_RIGHT)return modifiers[1];
            }
            return method.invoke(original,args);
        });
        int count=0;
        try {
            seed(editor,"print|"); processor.keyTyped('(');
            check(editor,"print(|)");count++;
            modifiers[0]=true;processor.keyDown(Keys.CONTROL_LEFT);processor.keyDown(Keys.Z);processor.keyUp(Keys.Z);processor.keyUp(Keys.CONTROL_LEFT);modifiers[0]=false;
            check(editor.getText().equals("print"),"native Undo retains text before auto-pair");count++;
            seed(editor,"for x in range(4):|");processor.keyDown(Keys.ENTER);processor.keyTyped('\r');processor.keyUp(Keys.ENTER);
            check(editor,"for x in range(4):\n    |");count++;
            processor.keyDown(Keys.TAB);processor.keyTyped('\t');processor.keyUp(Keys.TAB);
            check(editor,"for x in range(4):\n        |");count++;
            check(editor.hasKeyboardFocus(),"Tab does not transfer focus");count++;
            modifiers[1]=true;processor.keyDown(Keys.SHIFT_LEFT);processor.keyDown(Keys.TAB);processor.keyTyped('\t');processor.keyUp(Keys.TAB);processor.keyUp(Keys.SHIFT_LEFT);modifiers[1]=false;
            check(editor,"for x in range(4):\n    |");count++;
            seed(editor,"if can_harvest():|");processor.keyTyped('\n');
            check(editor,"if can_harvest():\n    |");count++;
            seed(editor,"print(|)");processor.keyTyped(')');check(editor,"print()|");count++;
            seed(editor,"print(|)");processor.keyTyped('\b');check(editor,"print|");count++;
            seed(editor,"    harvest()|");processor.keyTyped('\n');check(editor,"    harvest()\n    |");count++;
            seed(editor,"name = \"ab|\"");processor.keyTyped('(');check(editor,"name = \"ab(|\"");count++;
            seed(editor,"a\nb\nc|");editor.setSelection(0,4);processor.keyTyped('\t');
            check(editor.getText().equals("    a\n    b\nc"),"Tab indents selected lines only");count++;
            modifiers[1]=true;processor.keyTyped('\t');modifiers[1]=false;
            check(editor.getText().equals("a\nb\nc"),"Shift Tab dedents selected lines");count++;
            String block="# a longer line of code\n".repeat(100);
            editor.setText(block);editor.selectAll();processor.keyTyped('\t');
            check(editor.getText().equals("    # a longer line of code\n".repeat(100)),"large block indentation preserves content");count++;
            seed(editor,"abc|");editor.setSelection(0,3);processor.keyTyped('(');
            check(editor.getText().equals("(abc)") && editor.getSelection().equals("abc"),"auto-pair wraps and retains selection");count++;
            seed(editor,"call(|)");processor.keyTyped('\n');check(editor,"call(\n    |\n)");count++;
            seed(editor,"    |");processor.keyTyped('\b');check(editor,"|");count++;
            check(!editor.getText().contains("\t"),"indentation contains spaces");count++;
        } finally {Gdx.input=original;}
        return count;
    }
    static void typeLoopReference(TextArea editor,InputProcessor processor) {
        seed(editor,"|");
        String keys="for x in range(4):\nharvest()\nif x < 3:\nmove(\"east\")";
        for(int i=0;i<keys.length();i++)processor.keyTyped(keys.charAt(i));
        check(editor.getText().equals(com.marsreboot.runtime.PuzzleDefinition.TWO.reference()),"typed for/if program auto-indents to executable reference");
    }
    private static void seed(TextArea editor,String marked) {
        int caret=marked.indexOf('|');editor.setText(marked.replace("|",""));editor.setCursorPosition(caret);editor.setSelection(caret,caret);
    }
    private static void check(TextArea editor,String expected) {
        String text=editor.getText();int caret=editor.getCursorPosition();
        check((text.substring(0,caret)+"|"+text.substring(caret)).equals(expected),"expected "+expected+" got "+text+" caret "+caret);
    }
    private static void check(boolean value,String message){if(!value)throw new AssertionError(message);}
}
