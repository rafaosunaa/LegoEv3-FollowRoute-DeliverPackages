/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.uja.ambientales;

/**
 *
 * @author iesdi
 */
public class Grid {

    private final int x;
    private final int y;

    public Grid(int x, int y) {
        this.x = x;
        this.y = y;
    }

    public int getX() {
        return x;
    }

    public int getY() {
        return y;
    }
    
    public boolean equals(Grid grid){
        return x == grid.x && y == grid.y;
    }

}
