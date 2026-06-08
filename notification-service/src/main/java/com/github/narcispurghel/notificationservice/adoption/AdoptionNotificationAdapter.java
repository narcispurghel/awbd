package com.github.narcispurghel.notificationservice.adoption;

import com.github.narcispurghel.common.adoption.event.AdoptionLifecycleEvents.AdoptionApproved;
import com.github.narcispurghel.common.adoption.event.AdoptionLifecycleEvents.AdoptionCancelled;
import com.github.narcispurghel.common.adoption.event.AdoptionLifecycleEvents.AdoptionRejected;
import com.github.narcispurghel.common.adoption.event.AdoptionLifecycleEvents.AdoptionSubmitted;

public interface AdoptionNotificationAdapter {

  void onAdoptionSubmitted(AdoptionSubmitted event);

  void onAdoptionApproved(AdoptionApproved event);

  void onAdoptionRejected(AdoptionRejected event);

  void onAdoptionCancelled(AdoptionCancelled event);
}
