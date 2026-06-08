package com.github.narcispurghel.notificationservice.adoption;

import com.github.narcispurghel.common.adoption.event.AdoptionLifecycleEvents.AdoptionApproved;
import com.github.narcispurghel.common.adoption.event.AdoptionLifecycleEvents.AdoptionCancelled;
import com.github.narcispurghel.common.adoption.event.AdoptionLifecycleEvents.AdoptionRejected;
import com.github.narcispurghel.common.adoption.event.AdoptionLifecycleEvents.AdoptionSubmitted;
import java.util.List;
import org.springframework.stereotype.Component;

@Component
public class AdoptionNotificationDispatcher {

  private final List<AdoptionNotificationAdapter> adapters;

  public AdoptionNotificationDispatcher(List<AdoptionNotificationAdapter> adapters) {
    this.adapters = List.copyOf(adapters);
  }

  public void dispatch(AdoptionSubmitted event) {
    adapters.forEach(adapter -> adapter.onAdoptionSubmitted(event));
  }

  public void dispatch(AdoptionApproved event) {
    adapters.forEach(adapter -> adapter.onAdoptionApproved(event));
  }

  public void dispatch(AdoptionRejected event) {
    adapters.forEach(adapter -> adapter.onAdoptionRejected(event));
  }

  public void dispatch(AdoptionCancelled event) {
    adapters.forEach(adapter -> adapter.onAdoptionCancelled(event));
  }
}
