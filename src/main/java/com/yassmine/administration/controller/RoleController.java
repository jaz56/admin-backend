package com.yassmine.administration.controller;

import com.yassmine.administration.model.Role;
import com.yassmine.administration.repository.RoleRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/roles")
public class RoleController {

    @Autowired
    private RoleRepository roleRepository;

    @GetMapping
    public List<Role> getAllRoles() {
        return roleRepository.findAll();
    }

    @PostMapping
    public Role createRole(@RequestBody Role role) {
        return roleRepository.save(role);
    }

    @DeleteMapping("/{id}")
    public void deleteRole(@PathVariable String id) {
        roleRepository.deleteById(id);
    }
    @PutMapping("/{id}")
    public Role updateRole(@PathVariable String id, @RequestBody Role role) {
        return roleRepository.findById(id).map(existing -> {
            existing.setLabel(role.getLabel());
            existing.setValue(role.getValue());
            return roleRepository.save(existing);
        }).orElseThrow();
    }
}