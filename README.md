# ALRADHY Discovery Science for Android TV

An offline Arabic science museum with seven wings, 108 exhibits and 56 interactive labs. The telecom wing includes a link-budget waterfall, Fresnel path profile, QAM constellation and GEO/LEO latency comparison.

## Controls

- On Android TV, use the D-pad to move and OK to open. On a slider, Left and Right change the value, holding a direction accelerates the change, and OK toggles fine adjustment. Back returns to the previous screen.
- With a mouse, hover or click a control, drag or scroll on a slider, and right-click to go back. In the experiment step, dragging the simulation changes its selected parameters.
- Use the lab's compare step or tools menu to save a baseline. **ماذا لو؟** shows it alongside the current simulation on the same clock.

## Build and verify

Use JDK 17, Gradle 8.9, Android SDK 34 and build-tools 34.0.0. Run `gradle :app:assembleDebug` for internal emulator testing. The feature-branch workflow builds the signed release, validates content and equations, and checks the final APK's package, version code and certificate against the installed V0.7.0 baseline before publishing.

The application ID is `com.alradhy.discovery15labs.museumv05` and this release uses versionCode 17. On its first launch the app copies legacy experimental settings and observations to stable IDs. It keeps the three existing SharedPreferences files available in their original form.

## Model references

- [ITU-R P.525, free-space attenuation](https://www.itu.int/rec/R-REC-P.525-5-202411-I/en)
- [ITU-R P.526, Fresnel zones and diffraction](https://www.itu.int/rec/R-REC-P.526)
- [ITU-R terrestrial point-to-point planning handbook](https://www.itu.int/dms_pub/itu-r/opb/hdb/R-HDB-54-2009-OAS-PDF-E.pdf)
- [MathWorks, uncoded AWGN symbol-error rates](https://www.mathworks.com/help/comm/ref/berawgn.html)
- [NASA, cosmic distances](https://science.nasa.gov/solar-system/cosmic-distances/)

The telecom presets are examples with editable inputs. The link model includes user-entered rain attenuation. The Fresnel profile uses one obstacle and an effective Earth radius. QAM assumes normalized square constellations and AWGN. The satellite latency lab computes an idealized four-leg propagation time plus user-entered overhead.
