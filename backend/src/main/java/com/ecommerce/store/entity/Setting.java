package com.ecommerce.store.entity;

import jakarta.persistence.*;

@Entity
@Table(name = "settings")
public class Setting {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String keyName;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String value;

    @Column(name = "setting_group", nullable = false)
    private String group = "config";

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getKeyName() { return keyName; }
    public void setKeyName(String keyName) { this.keyName = keyName; }
    public String getValue() { return value; }
    public void setValue(String value) { this.value = value; }
    public String getGroup() { return group; }
    public void setGroup(String group) { this.group = group; }
}
