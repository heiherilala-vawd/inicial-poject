package com.example.demo.service.utils;

import com.example.demo.model.BoundedPageSize;
import com.example.demo.model.PageFromOne;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

public class PageUtils {
  public static Pageable createPageable(PageFromOne page, BoundedPageSize pageSize) {
    if (page == null) {
      page = new PageFromOne("1");
    }
    if (pageSize == null) {
      pageSize = new BoundedPageSize("15");
    }
    return PageRequest.of(page.getValue() - 1, pageSize.getValue());
  }
}
