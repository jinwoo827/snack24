package com.snack24.order.web;

import com.snack24.order.exception.OrderErrorCode;
import com.snack24.order.exception.OrderException;
import org.springframework.core.MethodParameter;
import org.springframework.lang.Nullable;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.support.WebDataBinderFactory;
import org.springframework.web.context.request.NativeWebRequest;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.method.support.ModelAndViewContainer;

@Component
public class CallerArgumentResolver implements HandlerMethodArgumentResolver {
    @Override
    public boolean supportsParameter(MethodParameter parameter) {
        return parameter.hasParameterAnnotation(Caller.class)
                && parameter.getParameterType().equals(CallerContext.class);
    }

    @Nullable
    @Override
    public Object resolveArgument(MethodParameter parameter, @Nullable ModelAndViewContainer mavContainer, NativeWebRequest webRequest, @Nullable WebDataBinderFactory binderFactory) throws Exception {

        String companyId = webRequest.getHeader("X-Company-Id");
        String memberId  = webRequest.getHeader("X-Member-Id");
        String role      = webRequest.getHeader("X-Member-Role");

        if (companyId == null || memberId == null) {
            throw new OrderException(OrderErrorCode.MISSING_CALLER_CONTEXT);
        }

        return new CallerContext(Long.valueOf(companyId), Long.valueOf(memberId), role);
    }
}
