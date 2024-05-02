package com.dongkuksystems.dbox.config.support;

public class SimpleOffsetPageRequest implements Pageable {
  
  private final long offset;

  private final int limit;

  public SimpleOffsetPageRequest() {
    this(0, 5);
  }

  public SimpleOffsetPageRequest(long offset, int limit) {
    if (offset < 0)
      throw new IllegalArgumentException("offset은 0 이상이어야 합니다.");
    if (limit < 1)
      throw new IllegalArgumentException("limit은 0보다 커야합니다.");
    this.offset = offset;
    this.limit = limit;
  }

  @Override
  public long offset() {
    return offset;
  }

  @Override
  public int limit() {
    return limit;
  }


}
