package edu.fosu.book.controller;

import edu.fosu.book.common.BusinessException;
import edu.fosu.book.common.Result;
import edu.fosu.book.dao.BookMapper;
import edu.fosu.book.dto.PageResult;
import edu.fosu.book.entity.Book;
import edu.fosu.book.service.BookService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/book")
public class BookController {

    @Autowired
    private BookService bookService;

    @Autowired
    private BookMapper bookMapper;

    @GetMapping("/{id}")
    public Result<Book> getBookById(@PathVariable String id) {
        Book book = bookService.selectByPrimaryKey(id);
        if (book != null) {
            return Result.success(book);
        } else {
            throw new BusinessException(404, "图书不存在");
        }
    }

    @GetMapping
    public Result<List<Book>> getAllBooks() {
        List<Book> books = bookService.getAllBooks();
        return Result.success(books);
    }

    @GetMapping("/page")
    public Result<PageResult<Book>> getBookPage(
            @RequestParam(defaultValue = "") String keyword,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size) {
        int offset = (page - 1) * size;
        List<Book> list = bookMapper.selectPage(keyword, offset, size);
        long total = bookMapper.countAll(keyword);
        return Result.success(new PageResult<>(list, total, page, size));
    }

    @PostMapping
    public Result<Book> insertBook(@RequestBody Book book) {
        Book savedBook = bookService.insertBook(book);
        if (savedBook != null) {
            return Result.success(savedBook);
        } else {
            throw new BusinessException("添加图书失败");
        }
    }

    @PutMapping("/{id}")
    public Result<Book> updateBookById(@PathVariable String id, @RequestBody Book book) {
        if (!id.equals(book.getBookid())) {
            throw new BusinessException(400, "ID不匹配");
        }

        Book existingBook = bookService.selectByPrimaryKey(id);
        if (existingBook == null) {
            throw new BusinessException(404, "图书不存在");
        }

        Book updatedBook = bookService.updateByPrimaryKey(book);
        if (updatedBook != null) {
            return Result.success(updatedBook);
        } else {
            throw new BusinessException("更新图书失败");
        }
    }

    @DeleteMapping("/{id}")
    public Result<Void> deleteBookById(@PathVariable String id) {
        Book existingBook = bookService.selectByPrimaryKey(id);
        if (existingBook == null) {
            throw new BusinessException(404, "图书不存在");
        }
        bookService.deleteByPrimaryKey(id);
        return Result.success();
    }
}
