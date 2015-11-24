package io.keiji.realmsample2.realm.entity;

import io.realm.RealmList;
import io.realm.RealmObject;

public class Characters extends RealmObject {

    private RealmList<Character> characters;

    public RealmList<Character> getCharacters() {
        return characters;
    }

    public void setCharacters(RealmList<Character> characters) {
        this.characters = characters;
    }
}
