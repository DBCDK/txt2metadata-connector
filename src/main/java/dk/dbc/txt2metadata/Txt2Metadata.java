/*
 * Copyright Dansk Bibliotekscenter a/s. Licensed under GPLv3
 * See license text in LICENSE.txt or at https://opensource.dbc.dk/licenses/gpl-3.0/
 */

package dk.dbc.txt2metadata;

public class Txt2Metadata {
    private String value;
    private int score;
    private String type;

    @Override
    public String toString() {
        return "Txt2Metadata{" +
                "value='" + value + '\'' +
                ", score=" + score +
                ", type='" + type + '\'' +
                '}';
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }

    public int getScore() {
        return score;
    }

    public void setScore(int score) {
        this.score = score;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }
}
