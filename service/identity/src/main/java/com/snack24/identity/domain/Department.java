package com.snack24.identity.domain;

import com.snack24.common.jpabase.config.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;

@Entity
@Getter
@ToString
@Table(name = "departments")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Department extends BaseEntity {
    @Id
    @Column(name = "department_id")
    private Long departmentId;

    @Column(name = "company_id", nullable = false)
    private Long companyId;

    @Column(name = "parent_department_id")
    private Long parentDepartmentId;

    @Column(name = "name", nullable = false, length = 100)
    private String name;

    @Column(name = "depth", nullable = false)
    private int depth;

    @Column(name = "path", length = 100)
    private String path;

    @Column(name = "display_order", nullable = false)
    private int displayOrder;

    public static Department createRoot(Long departmentId, Long companyId, String name, int displayOrder) {
        Department department = new Department();
        department.departmentId = departmentId;
        department.companyId = companyId;
        department.parentDepartmentId = null;
        department.name = name;
        department.displayOrder = displayOrder;
        department.depth = 0;
        department.path = "/" + departmentId + "/";
        return department;
    }

    public static Department createChild(Long departmentId, Department parent, String name, int displayOrder) {
        Department department = new Department();
        department.departmentId = departmentId;
        department.companyId = parent.companyId;
        department.parentDepartmentId = parent.departmentId;
        department.name = name;
        department.displayOrder = displayOrder;
        department.depth = parent.depth + 1;
        department.path = parent.path + departmentId + "/";
        return department;
    }

    public void rename(String newName) {
        this.name = newName;
    }

    public void changeDisplayOrder(int displayOrder) {
        this.displayOrder = displayOrder;
    }
}
