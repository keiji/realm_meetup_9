package io.keiji.realmsample2.realm.entity;

import net.vvakame.util.jsonpullparser.annotation.JsonKey;
import net.vvakame.util.jsonpullparser.annotation.JsonModel;

import io.keiji.realmsample2.realm.util.AgeTokenConverter;
import io.realm.RealmObject;

    @JsonModel
    public class Character extends RealmObject {

        @JsonKey
        private long id;

        @JsonKey
        private String name;

        @JsonKey(converter = AgeTokenConverter.class)
        private Integer age;

        @JsonKey
        private boolean megane = true;

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Integer getAge() {
        return age;
    }

    public void setAge(Integer age) {
        this.age = age;
    }

    public boolean isMegane() {
        return megane;
    }

    public void setMegane(boolean megane) {
        this.megane = megane;
    }

}
