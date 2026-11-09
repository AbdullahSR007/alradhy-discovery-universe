# ALRADHY Discovery Museum V0.5

This build **preserves the previous releases**: the feature branch is separate, and
the V0.5 application ID differs from earlier installed APKs.

## What V0.5 includes

- 50 history/cosmos educational stations with thematic links to appropriate labs.
- Three independently implemented light-speed labs: Galileo's lantern timing,
  Rømer's relative Io-eclipse delay, and Fizeau's rotating wheel.
- Twelve additional interactive laboratories: Newton's prism, Foucault's pendulum,
  Coulomb's law, orbital cannon, Young's double slit, buoyancy, Faraday induction,
  Ohm's law, Kepler's orbits, Doppler shift, Galileo's pendulum, Einstein's
  photoelectric effect.
- Guided inquiry ("learn with me") and open exploration modes; historical context,
  current applications and explanation of **limitations**.
- Interactive numerical outputs; graph plotting an observable vs the first control
  while holding the second constant (12 newer labs).
- Screen drag, mouse, DPAD and explicit increment/decrement/preset buttons
  (the 12 newer labs). The three light labs have mouse drag and DPAD controls.
- Save comparison baseline and compare before vs after; resume labs on restart.
- Completion progress and science challenges for the twelve newer labs.

## Distinguishing scientific modeling from experiments

Every lab is a mathematical/educational simulation and does **not** collect real
measurements or claim laboratory-equipment accuracy. Individual assumptions and
limits are on-screen. The Rømer model holds the underlying event time fixed,
varying only light-travel delay, not a full Jovian ephemeris. The Fizeau model
idealizes the tooth wheel and 8,633 m one-way distance. A schematic drawing must
not be interpreted as a to-scale rendering of an experiment.

## Validation

The GitHub workflow runs scripts/check_science_models.py, the Android Gradle
build, APK non-empty verification and SHA-256. This establishes source/build
sanity, **not** on-device QA. Testing on an actual Android TV box remains necessary.
In particular, validate all 15 screens, focus traversal, scrolling, mouse drag,
persisted state, comparisons, and display readability.

The preview APK uses debug signing and a separate applicationId:
com.alradhy.discovery15labs.museumv05. Stable signed in-place updates require
a securely retained signing certificate in GitHub Actions secrets before the
app adopts a persistent production ID.
