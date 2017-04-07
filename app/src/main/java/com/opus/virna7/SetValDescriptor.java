package com.opus.virna7;

/**
 * Created by opus on 14/01/17.
 */

public class SetValDescriptor {

    private int type;
    private String title;
    private String prompt;
    private String unit;
    private int icon;
    private int min;
    private int max;
    private int value;
    private int direction;



    public SetValDescriptor() {

    }


    public String getTitle() {
        return title;
    }

    public SetValDescriptor setTitle(String title) {
        this.title = title;
        return this;
    }

    public int getIcon() {
        return icon;
    }

    public SetValDescriptor setIcon(int icon) {
        this.icon = icon;
        return this;
    }

    public int getMin() {
        return min;
    }

    public SetValDescriptor setMin(int min) {
        this.min = min;
        return this;
    }

    public int getMax() {
        return max;
    }

    public SetValDescriptor setMax(int max) {
        this.max = max;
        return this;
    }

    public int getValue() {
        return value;
    }

    public  SetValDescriptor setValue(int value) {
        this.value = value;
        return this;
    }

    public String getPrompt() {
        return prompt;
    }

    public  SetValDescriptor setPrompt(String prompt) {
        this.prompt = prompt;
        return this;
    }

    public String getUnit() {
        return unit;
    }

    public  SetValDescriptor setUnit(String unit) {
        this.unit = unit;
        return this;
    }

    public int getType() {
        return type;
    }

    public  SetValDescriptor setType(int type) {
        this.type = type;
        return this;
    }

    public int getDirection() {
        return direction;
    }

    public  SetValDescriptor setDirection(int dir) {
        this.direction = dir;
        return this;
    }
}
