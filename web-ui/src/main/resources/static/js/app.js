// Bootstrap bundle handles most interactive components; this file adds a small
// progressive-enhancement script for filtering a breed <select> by species.
//
// Wire-up: put data-breed-filter-source on the species <select> and
// data-breed-filter-target on the breed <select>; each breed <option> must
// carry data-species-id="...".

// Client-side validation (Bootstrap "needs-validation" pattern): on submit,
// block the request and reveal field-level messages when any HTML5 constraint
// (required, type, pattern, minlength, maxlength, min...) fails. Server-side
// Bean Validation still runs as the authoritative check.
(function () {
  document.addEventListener('DOMContentLoaded', function () {
    var forms = document.querySelectorAll('form.needs-validation');
    Array.prototype.forEach.call(forms, function (form) {
      form.addEventListener(
        'submit',
        function (event) {
          if (!form.checkValidity()) {
            event.preventDefault();
            event.stopPropagation();
          }
          form.classList.add('was-validated');
        },
        false
      );
    });
  });
})();

(function () {
  function applyFilter(speciesSelect, breedSelect) {
    var selectedSpeciesId = speciesSelect.value;
    var options = breedSelect.options;
    var selectedBreedHidden = false;
    for (var i = 0; i < options.length; i++) {
      var opt = options[i];
      if (!opt.value) {
        opt.hidden = false;
        opt.disabled = false;
        continue;
      }
      var optSpecies = opt.getAttribute('data-species-id');
      var match = !!selectedSpeciesId && optSpecies === selectedSpeciesId;
      opt.hidden = !match;
      opt.disabled = !match;
      if (opt.selected && !match) {
        selectedBreedHidden = true;
      }
    }
    if (selectedBreedHidden) {
      breedSelect.value = '';
    }
  }

  document.addEventListener('DOMContentLoaded', function () {
    var speciesSelect = document.querySelector('[data-breed-filter-source]');
    var breedSelect = document.querySelector('[data-breed-filter-target]');
    if (!speciesSelect || !breedSelect) {
      return;
    }
    applyFilter(speciesSelect, breedSelect);
    speciesSelect.addEventListener('change', function () {
      applyFilter(speciesSelect, breedSelect);
    });
  });
})();

// Cancel-adoption confirmation modal: when shown, copies request id + animal
// name from the triggering button into the modal's form action and body.
(function () {
  document.addEventListener('DOMContentLoaded', function () {
    var modal = document.getElementById('cancelRequestModal');
    if (!modal) {
      return;
    }
    var form = modal.querySelector('[data-cancel-request-form]');
    var animalLabel = modal.querySelector('[data-cancel-request-animal]');
    if (!form || !animalLabel) {
      return;
    }
    var baseAction = form.getAttribute('action') || '';
    modal.addEventListener('show.bs.modal', function (event) {
      var trigger = event.relatedTarget;
      if (!trigger) {
        return;
      }
      var requestId = trigger.getAttribute('data-request-id') || '';
      var animalName = trigger.getAttribute('data-animal-name') || 'this animal';
      form.setAttribute('action', baseAction.replace('placeholder', requestId));
      animalLabel.textContent = animalName;
    });
  });
})();
