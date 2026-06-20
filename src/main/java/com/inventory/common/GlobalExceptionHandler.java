package com.inventory.common;

import com.inventory.common.exception.BusinessException;
import jakarta.validation.ConstraintViolationException;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

import java.sql.SQLIntegrityConstraintViolationException;
import java.util.HashMap;
import java.util.Map;

@RestControllerAdvice
public class GlobalExceptionHandler {

    // 1. 处理 @Valid 校验失败
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public Result<Map<String, String>> handleValidationExceptions(MethodArgumentNotValidException ex) {
        Map<String, String> errors = new HashMap<>();
        ex.getBindingResult().getAllErrors().forEach((error) -> {
            String fieldName = ((FieldError) error).getField();
            String errorMessage = error.getDefaultMessage();
            errors.put(fieldName, errorMessage);
        });
        return Result.error(400, "参数校验失败", errors);
    }

    // 2. 处理单个参数校验失败
    @ExceptionHandler(ConstraintViolationException.class)
    public Result<String> handleConstraintViolation(ConstraintViolationException ex) {
        String message = ex.getConstraintViolations().iterator().next().getMessage();
        return Result.error(400, message);
    }

    // 3. 处理业务异常（主动抛出的异常）
    @ExceptionHandler(BusinessException.class)
    public Result<String> handleBusinessException(BusinessException ex) {
        return Result.error(ex.getCode(), ex.getMessage());
    }

    // 4. 处理参数缺失（如 @RequestParam 没传）
    @ExceptionHandler(MissingServletRequestParameterException.class)
    public Result<String> handleMissingParam(MissingServletRequestParameterException ex) {
        return Result.error(400, "缺少必要参数: " + ex.getParameterName());
    }

    // 5. 处理参数类型错误（如传字符串给 int 参数）
    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public Result<String> handleTypeMismatch(MethodArgumentTypeMismatchException ex) {
        return Result.error(400, "参数类型错误: " + ex.getName() + " 应为 " + ex.getRequiredType().getSimpleName());
    }

    // 6. 处理 JSON 格式错误
    @ExceptionHandler(HttpMessageNotReadableException.class)
    public Result<String> handleJsonParseError(HttpMessageNotReadableException ex) {
        return Result.error(400, "请求体格式错误，请检查 JSON 格式");
    }

    // 7. 处理唯一索引冲突（重复插入）
    @ExceptionHandler(DuplicateKeyException.class)
    public Result<String> handleDuplicateKey(DuplicateKeyException ex) {
        return Result.error(409, "数据已存在，不允许重复添加");
    }

    // 8. 处理数据库完整性约束异常（如外键关联、非空字段）
    @ExceptionHandler(DataIntegrityViolationException.class)
    public Result<String> handleDataIntegrityViolation(DataIntegrityViolationException ex) {
        return Result.error(500, "数据完整性约束失败，请检查关联数据是否存在");
    }

    // 9. 处理通用 SQL 异常（兜底）
    @ExceptionHandler(SQLIntegrityConstraintViolationException.class)
    public Result<String> handleSQLIntegrityConstraintViolation(SQLIntegrityConstraintViolationException ex) {
        return Result.error(500, "数据库操作失败：" + ex.getMessage());
    }

    // 10. 处理空指针（兜底）
    @ExceptionHandler(NullPointerException.class)
    public Result<String> handleNullPointer(NullPointerException ex) {
        return Result.error(500, "系统异常：空指针引用，请联系管理员");
    }

    // 11. 处理所有未捕获的异常（最终兜底）
    @ExceptionHandler(Exception.class)
    public Result<String> handleException(Exception ex) {
        // 打印完整堆栈（方便排查）
        ex.printStackTrace();
        return Result.error(500, "系统异常：" + ex.getMessage());
    }
}