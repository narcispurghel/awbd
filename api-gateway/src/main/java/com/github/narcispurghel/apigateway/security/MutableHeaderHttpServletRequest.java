package com.github.narcispurghel.apigateway.security;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletRequestWrapper;
import java.util.Collections;
import java.util.Enumeration;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

/** Adds or overrides request headers for downstream proxying. */
final class MutableHeaderHttpServletRequest extends HttpServletRequestWrapper {

  private final Map<String, String> headers = new ConcurrentHashMap<>();

  MutableHeaderHttpServletRequest(HttpServletRequest request) {
    super(request);
  }

  void setHeader(String name, String value) {
    headers.put(name, value);
  }

  @Override
  public String getHeader(String name) {
    String value = headers.get(name);
    if (value != null) {
      return value;
    }
    return super.getHeader(name);
  }

  @Override
  public Enumeration<String> getHeaders(String name) {
    if (headers.containsKey(name)) {
      return Collections.enumeration(Set.of(headers.get(name)));
    }
    return super.getHeaders(name);
  }

  @Override
  public Enumeration<String> getHeaderNames() {
    Set<String> names = new LinkedHashSet<>(headers.keySet());
    Enumeration<String> original = super.getHeaderNames();
    while (original.hasMoreElements()) {
      names.add(original.nextElement());
    }
    return Collections.enumeration(names);
  }
}
