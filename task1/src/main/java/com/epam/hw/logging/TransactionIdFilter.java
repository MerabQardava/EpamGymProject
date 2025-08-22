package com.epam.hw.logging;

import jakarta.servlet.*;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.MDC;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.UUID;

@Component
public class TransactionIdFilter implements Filter {

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {

        String txId = UUID.randomUUID().toString().substring(0, 8);
        MDC.put("txId", txId);


        ((HttpServletResponse) response).setHeader("X-Transaction-ID", txId);
        request.setAttribute("X-Transaction-ID", txId);
        try {
            chain.doFilter(request, response);
        } finally {
            MDC.clear();
        }
    }
}
