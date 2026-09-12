package com.smartcampus.dto;

public class ProgramResponseDTO {

    private Long id;
    private String name;
    private String code;
    private String description;

    private Long departmentId;
    private String departmentName;
    private String departmentCode;

    public ProgramResponseDTO() {
    }

    public ProgramResponseDTO(
            Long id,
            String name,
            String code,
            String description,
            Long departmentId,
            String departmentName,
            String departmentCode) {

        this.id = id;
        this.name = name;
        this.code = code;
        this.description = description;
        this.departmentId = departmentId;
        this.departmentName = departmentName;
        this.departmentCode = departmentCode;
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

    public Long getDepartmentId() {
        return departmentId;
    }

    public String getDepartmentName() {
        return departmentName;
    }

    public String getDepartmentCode() {
        return departmentCode;
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

    public void setDepartmentId(Long departmentId) {
        this.departmentId = departmentId;
    }

    public void setDepartmentName(String departmentName) {
        this.departmentName = departmentName;
    }

    public void setDepartmentCode(String departmentCode) {
        this.departmentCode = departmentCode;
    }
}