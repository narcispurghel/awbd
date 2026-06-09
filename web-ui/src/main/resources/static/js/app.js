// Bootstrap bundle handles most interactive components; this file adds a small
// progressive-enhancement script for filtering a breed <select> by species.
//
// Wire-up: put data-breed-filter-source on the species <select> and
// data-breed-filter-target on the breed <select>; each breed <option> must
// carry data-species-id="...".
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
