package edu.fosu.book.controller;

import edu.fosu.book.common.BusinessException;
import edu.fosu.book.common.Result;
import edu.fosu.book.dao.BorrowMapper;
import edu.fosu.book.dto.PageResult;
import edu.fosu.book.entity.Borrow;
import edu.fosu.book.service.BorrowService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/borrow")
public class BorrowController {

    @Autowired
    private BorrowService borrowService;

    @Autowired
    private BorrowMapper borrowMapper;

    @GetMapping("/{userid}/{bookid}")
    public Result<Borrow> getBorrowByPrimaryKey(@PathVariable String userid, @PathVariable String bookid) {
        Borrow borrow = borrowService.selectByPrimaryKey(userid, bookid);
        if (borrow != null) {
            return Result.success(borrow);
        } else {
            throw new BusinessException(404, "借阅记录不存在");
        }
    }

    @GetMapping
    public Result<List<Borrow>> getAllBorrows() {
        List<Borrow> borrows = borrowService.getAllBorrows();
        return Result.success(borrows);
    }

    @GetMapping("/page")
    public Result<PageResult<Borrow>> getBorrowPage(
            @RequestParam(defaultValue = "") String keyword,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size) {
        int offset = (page - 1) * size;
        List<Borrow> list = borrowMapper.selectPage(keyword, offset, size);
        long total = borrowMapper.countAll(keyword);
        return Result.success(new PageResult<>(list, total, page, size));
    }

    @PostMapping
    public Result<Borrow> insertBorrow(@RequestBody Borrow borrow) {
        Borrow savedBorrow = borrowService.insertBorrow(borrow);
        if (savedBorrow != null) {
            return Result.success(savedBorrow);
        } else {
            throw new BusinessException("添加借阅记录失败");
        }
    }

    @PutMapping("/{userid}/{bookid}")
    public Result<Borrow> updateBorrowByPrimaryKey(@PathVariable String userid, @PathVariable String bookid, @RequestBody Borrow borrow) {
        if (!userid.equals(borrow.getUserid()) || !bookid.equals(borrow.getBookid())) {
            throw new BusinessException(400, "ID不匹配");
        }

        Borrow existingBorrow = borrowService.selectByPrimaryKey(userid, bookid);
        if (existingBorrow == null) {
            throw new BusinessException(404, "借阅记录不存在");
        }

        Borrow updatedBorrow = borrowService.updateByPrimaryKey(borrow);
        if (updatedBorrow != null) {
            return Result.success(updatedBorrow);
        } else {
            throw new BusinessException("更新借阅记录失败");
        }
    }

    @DeleteMapping("/{userid}/{bookid}")
    public Result<Void> deleteBorrowByPrimaryKey(@PathVariable String userid, @PathVariable String bookid) {
        Borrow existingBorrow = borrowService.selectByPrimaryKey(userid, bookid);
        if (existingBorrow == null) {
            throw new BusinessException(404, "借阅记录不存在");
        }
        borrowService.deleteByPrimaryKey(userid, bookid);
        return Result.success();
    }
}
