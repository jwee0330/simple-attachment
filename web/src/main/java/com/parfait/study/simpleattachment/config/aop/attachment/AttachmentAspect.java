package com.parfait.study.simpleattachment.config.aop.attachment;

import com.parfait.study.simpleattachment.config.aop.attachment.service.AttachService;
import com.parfait.study.simpleattachment.config.interceptor.attachment.AttachmentTypeHolder;
import com.parfait.study.simpleattachment.shared.model.attachment.AttachmentType;
import lombok.NonNull;
import org.aspectj.lang.annotation.AfterReturning;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@Component
@Aspect
public class AttachmentAspect {

    private final AttachmentTypeHolder attachmentTypeHolder;
    private final Map<AttachmentType, List<AttachService>> typeToServiceMap;

    @Autowired
    public AttachmentAspect(@NonNull AttachmentTypeHolder attachmentTypeHolder,
                            @NonNull List<AttachService> attachService) {
        this.attachmentTypeHolder = attachmentTypeHolder;
        this.typeToServiceMap = attachService.stream()
                                             .collect(Collectors.groupingBy(AttachService::getSupportAttachmentType, Collectors.toList()));
    }

    @Pointcut("@annotation(com.parfait.study.simpleattachment.config.interceptor.attachment.Attachable)")
    private void pointcut() {
    }

    @AfterReturning(pointcut = "pointcut()", returning = "returnValue")
    public Object afterReturning(Object returnValue) {

        if (attachmentTypeHolder.isEmpty()) {
            return returnValue;
        }

        Set<AttachmentType> types = attachmentTypeHolder.getTypes();
        types.stream()
             .flatMap(type -> typeToServiceMap.get(type).stream())
             .filter(service -> service.supports(returnValue))
             .forEach(service -> service.attach(returnValue));

        return returnValue;
    }
}
