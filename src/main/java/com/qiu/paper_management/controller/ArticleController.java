package com.qiu.paper_management.controller;

import com.qiu.paper_management.pojo.*;
import com.qiu.paper_management.service.ArticleService;
import com.qiu.paper_management.utils.ThreadLocalUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.Objects;

@CrossOrigin
@RestController
@RequestMapping("/article")
public class ArticleController {

    @Autowired
    private ArticleService articleService;


    @PostMapping
    public Result postArticle(@RequestBody @Validated(Article.Add.class) Article_Author articleWithAuthor){
        if (articleService.duplicate(articleWithAuthor.getArticle().getTitle()))
            throw new RuntimeException("同名文章已存在!");
        articleService.postArticle(articleWithAuthor);
        return Result.success();
    }


    @GetMapping
    public Result<PageBean<Article>> list(
            @RequestParam Integer pageNum,
            @RequestParam Integer pageSize,
            @RequestParam(required = false) Integer categoryId
    )
    {
        PageBean<Article> pb = articleService.list(pageNum, pageSize, categoryId);
        return Result.success(pb);
    }

    @GetMapping("/detail")
    public Result<Article> getDetail(@RequestParam Integer id){
        Article article = articleService.getDetail(id);
        if(article == null)
            throw new RuntimeException("文章不存在或已经被删除");
        return Result.success(article);
    }

    @PutMapping
    public Result updateArticle(@RequestBody @Validated(Article.Update.class) Article article){
        // 1. 确保是自己的文章
        authorization(article.getId());

        articleService.update(article);
        return Result.success();
    }

    // 确保文章存在且删除的是自己的文章
    void authorization(Integer id){
        if (articleService.getDetail(id) == null)
            throw new RuntimeException("文章不存在或已经被删除");
        Integer authorId = ThreadLocalUtil.getId();
        // 如果不是作者之一
        if (!articleService.findAuthorsById(id).contains(authorId)){
            throw new RuntimeException("您不是论文作者，无权删除该论文！");
        }

    }

    @DeleteMapping
    public Result deleteArticle(@RequestParam Integer id){
        this.authorization(id);
        articleService.deleteArticle(id);
        return Result.success();
    }

}
