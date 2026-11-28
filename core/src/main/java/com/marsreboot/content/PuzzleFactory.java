package com.marsreboot.content;

public abstract class PuzzleFactory<T> {

    public abstract PuzzleDefinition createPuzzle(T config);
}
