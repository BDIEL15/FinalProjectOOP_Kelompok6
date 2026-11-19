package com.marsreboot.habitat;

/** Dependency-free behavioral checks using | as the caret marker. */
public final class PythonEditingTest {
    private static int count;
    public static void main(String[] args) {
        edit("for x in range(4):|", '\n', false, "for x in range(4):\n    |");
        edit("    if can_harvest(): # check|", '\r', false, "    if can_harvest(): # check\n        |");
        edit("    harvest()|", '\n', false, "    harvest()\n    |");
        edit("print('not a block:')|", '\n', false, "print('not a block:')\n|");
        edit("# for x in range(4):|", '\n', false, "# for x in range(4):\n|");
        edit("call(\n    item,|", '\n', false, "call(\n    item,\n    |");
        edit("\tif x:|", '\n', false, "\tif x:\n\t    |");
        edit("a|", '\t', false, "a   |");
        edit("    |", '\t', false, "        |");
        edit("        harvest()|", '\t', true, "    harvest()|");
        edit("      |", '\b', false, "    |");
        edit("print|", '(', false, "print(|)");
        edit("items = |", '[', false, "items = [|]");
        edit("data = |", '{', false, "data = {|}");
        edit("print(|)", ')', false, "print()|");
        edit("print(|)", '\b', false, "print|");
        edit("print(|)", '\n', false, "print(\n    |\n)");
        edit("value = |", '"', false, "value = \"|\"");
        edit("value = \"text|\"", '"', false, "value = \"text\"|");
        check(PythonEditing.edit("# (", 3, 3, '(', false) == null, "comment not paired");
        check(PythonEditing.edit("'hello", 6, 6, '(', false) == null, "string not paired");
        check(PythonEditing.edit("abc", 1, 1, '(', false) == null, "middle of identifier not paired");
        var wrap = PythonEditing.edit("abc", 0, 3, '(', false);
        check(wrap.text().equals("(abc)") && wrap.anchor()==1 && wrap.cursor()==4,"wrap selection");
        var indent = PythonEditing.edit("a\nb\nc", 0, 4, '\t', false);
        check(indent.text().equals("    a\n    b\nc"),"selection ending at next line excludes that line");
        var outdent = PythonEditing.edit(indent.text(), indent.anchor(), indent.cursor(), '\t', true);
        check(outdent.text().equals("a\nb\nc"),"multiline dedent restores text");
        System.out.println("PASS: " + count + " Python editor transformation assertions.");
    }
    static void edit(String before,char key,boolean shift,String expected) {
        int pos=before.indexOf('|'); String text=before.replace("|", "");
        var result=PythonEditing.edit(text,pos,pos,key,shift);
        check(result!=null && (result.text().substring(0,result.cursor())+"|"+result.text().substring(result.cursor())).equals(expected),before+" -> "+result);
    }
    static void check(boolean value,String message) { if(!value)throw new AssertionError(message);count++; }
}
