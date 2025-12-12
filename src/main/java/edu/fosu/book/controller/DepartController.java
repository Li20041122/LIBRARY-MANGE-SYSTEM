package edu.fosu.book.controller;

import edu.fosu.book.common.BusinessException;
import edu.fosu.book.common.Result;
import edu.fosu.book.dao.DepartMapper;
import edu.fosu.book.dto.PageResult;
import edu.fosu.book.entity.Depart;
import edu.fosu.book.service.DepartService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/depart")
public class DepartController {

    @Autowired
    private DepartService departService;

    @Autowired
    private DepartMapper departMapper;

    @GetMapping("/{id}")
    public Result<Depart> getDepartById(@PathVariable String id) {
        Depart depart = departService.selectByPrimaryKey(id);
        if (depart != null) {
            return Result.success(depart);
        } else {
            throw new BusinessException(404, "部门不存在");
        }
    }

    @GetMapping
    public Result<List<Depart>> getAllDeparts() {
        List<Depart> departs = departService.getAllDeparts();
        return Result.success(departs);
    }

    @GetMapping("/page")
    public Result<PageResult<Depart>> getDepartPage(
            @RequestParam(defaultValue = "") String keyword,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size) {
        int offset = (page - 1) * size;
        List<Depart> list = departMapper.selectPage(keyword, offset, size);
        long total = departMapper.countAll(keyword);
        return Result.success(new PageResult<>(list, total, page, size));
    }

    @PostMapping
    public Result<Depart> insertDepart(@RequestBody Depart depart) {
        // 空字符串转为null，避免外键约束错误
        if (depart.getParentdepartid() != null && depart.getParentdepartid().isEmpty()) {
            depart.setParentdepartid(null);
        }
        Depart savedDepart = departService.insertDepart(depart);
        if (savedDepart != null) {
            return Result.success(savedDepart);
        } else {
            throw new BusinessException("添加部门失败");
        }
    }

    @PutMapping("/{id}")
    public Result<Depart> updateDepartById(@PathVariable String id, @RequestBody Depart depart) {
        if (!id.equals(depart.getDepartid())) {
            throw new BusinessException(400, "ID不匹配");
        }

        Depart existingDepart = departService.selectByPrimaryKey(id);
        if (existingDepart == null) {
            throw new BusinessException(404, "部门不存在");
        }

        // 空字符串转为null，避免外键约束错误
        if (depart.getParentdepartid() != null && depart.getParentdepartid().isEmpty()) {
            depart.setParentdepartid(null);
        }

        Depart updatedDepart = departService.updateByPrimaryKey(depart);
        if (updatedDepart != null) {
            return Result.success(updatedDepart);
        } else {
            throw new BusinessException("更新部门失败");
        }
    }

    @DeleteMapping("/{id}")
    public Result<Void> deleteDepartById(@PathVariable String id) {
        Depart existingDepart = departService.selectByPrimaryKey(id);
        if (existingDepart == null) {
            throw new BusinessException(404, "部门不存在");
        }
        departService.deleteByPrimaryKey(id);
        return Result.success();
    }
}
