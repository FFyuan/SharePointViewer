package edu.rosehulman.yuanx.sharepointviewer;

/**
 * Created by yuanx on 8/16/2015.
 */
public class MenuItems {
    private String name;
    private boolean isHeader;

    public MenuItems(String name, boolean isHeader){
        this.name = name;
        this.isHeader = isHeader;
    }

    public String getName() {
        return name;
    }

    public boolean isHeader() {
        return isHeader;
    }
}
