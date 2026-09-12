package com.smartcampus.dto;

public class DepartmentResponseDTO {

    private Long id;
    private String name;
    private String code;
    private String description;

    public DepartmentResponseDTO() {
    }

    public DepartmentResponseDTO(
            Long id,
            String name,
            String code,
            String description) {

        this.id = id;
        this.name = name;
        this.code = code;
        this.description = description;
    }

    public Long getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getCode() {
        return code;
    }

    public String getDescription() {
        return description;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public void setDescription(String description) {
        this.description = description;
    }
}