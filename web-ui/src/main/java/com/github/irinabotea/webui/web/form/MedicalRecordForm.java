package com.github.irinabotea.webui.web.form;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.math.BigDecimal;
import java.time.LocalDate;
import org.jspecify.annotations.Nullable;
import org.springframework.format.annotation.DateTimeFormat;

public class MedicalRecordForm {

  @NotBlank
  @Size(max = 120)
  private String title = "";

  @NotNull
  @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
  private @Nullable LocalDate examinationDate = LocalDate.now();

  @Size(max = 255)
  private @Nullable String treatment;

  @Size(max = 2000)
  private @Nullable String notes;

  @DecimalMin("0.0")
  private @Nullable BigDecimal weightKg;

  private boolean followUpRequired;

  public String getTitle() { return title; }
  public void setTitle(String title) { this.title = title; }

  public @Nullable LocalDate getExaminationDate() { return examinationDate; }
  public void setExaminationDate(@Nullable LocalDate examinationDate) { this.examinationDate = examinationDate; }

  public @Nullable String getTreatment() { return treatment; }
  public void setTreatment(@Nullable String treatment) { this.treatment = treatment; }

  public @Nullable String getNotes() { return notes; }
  public void setNotes(@Nullable String notes) { this.notes = notes; }

  public @Nullable BigDecimal getWeightKg() { return weightKg; }
  public void setWeightKg(@Nullable BigDecimal weightKg) { this.weightKg = weightKg; }

  public boolean isFollowUpRequired() { return followUpRequired; }
  public void setFollowUpRequired(boolean followUpRequired) { this.followUpRequired = followUpRequired; }
}
