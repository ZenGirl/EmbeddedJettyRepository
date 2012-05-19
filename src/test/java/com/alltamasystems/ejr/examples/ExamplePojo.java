package com.alltamasystems.ejr.examples;

import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement
public class ExamplePojo {

    private String words;
    private int    number;

    public ExamplePojo() {
    }

    public int getNumber() {
        return number;
    }

    public void setNumber(int number) {
        this.number = number;
    }

    public String getWords() {
        return words;
    }

    public void setWords(String words) {
        this.words = words;
    }
}
