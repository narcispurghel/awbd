package com.github.irinabotea.webui.web.form;

import com.github.irinabotea.webui.client.BackendDtos.AdoptionDtos;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import org.jspecify.annotations.Nullable;

public class ReviewForm {

  @NotNull
  private AdoptionDtos.@Nullable AdoptionRequestStatus status;

  @Size(max = 1000)
  private @Nullable String reviewNote;

  public AdoptionDtos.@Nullable AdoptionRequestStatus getStatus() {
    return status;
  }

  public void setStatus(AdoptionDtos.@Nullable AdoptionRequestStatus status) {
    this.status = status;
  }

  public @Nullable String getReviewNote() {
    return reviewNote;
  }

  public void setReviewNote(@Nullable String reviewNote) {
    this.reviewNote = reviewNote;
  }
}
