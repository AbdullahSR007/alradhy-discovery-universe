# ALRADHY Discovery Science for Android TV

An offline Arabic science museum with seven wings, 187 exhibits and 118 interactive labs. The telecom wing contains 44 interactive labs organized around RAN, RF, optics, transport, and modulation. The new models cover SINR, RSRQ, NR resource-grid payload, receiver sensitivity, cell-edge coverage, rain attenuation, optical loss, dispersion, VSWR, EIRP, downtilt, packet delay, availability, and summing powers.

The biology wing now includes 20 labs and a 17-stop journey from LUCA to modern humans. Each stop distinguishes dated evidence from interpretation, links to a related experiment, and credits its scientific source. The illustrated panels are interpretive museum art. See [art credits and exact generation prompts](EXHIBIT-ART.md).

## Controls

- On Android TV, use the D-pad to move and OK to open. On a slider, Left and Right change the value, holding a direction accelerates the change, and OK toggles fine adjustment. Back returns to the previous screen.
- With a mouse, hover or click a control, drag or scroll on a slider, and right-click to go back. In the experiment step, dragging the simulation changes its selected parameters.
- Use the lab's compare step or tools menu to save a baseline. **ماذا لو؟** shows it alongside the current simulation on the same clock.

## Build and verify

Use JDK 17, Gradle 8.9, Android SDK 34 and build-tools 34.0.0. Run `gradle :app:assembleDebug` for internal emulator testing. The feature-branch workflow builds the signed release, validates content and equations, and checks the final APK's package, version code and certificate against the installed 1.1.0 baseline before publishing.

The application ID is `com.alradhy.discovery15labs.museumv05` and this release uses versionCode 19. On its first launch the app copies legacy experimental settings and observations to stable IDs. It keeps the three existing SharedPreferences files available in their original form.

Every graph uses the live measurement model. The X-axis control switches the swept input while each series keeps its own other inputs fixed. Discrete controls plot points. A missing physical reading leaves a gap, such as a focused thin lens or total internal reflection. The CI sweep test runs every axis under default, minimum and maximum configurations.

## Model references

- [Moody et al., LUCA and the early Earth system](https://www.nature.com/articles/s41559-024-02461-1)
- [Smithsonian Human Origins Program](https://humanorigins.si.edu/education/introduction-human-evolution)
- [Hublin et al., early Homo sapiens fossils at Jebel Irhoud](https://www.nature.com/articles/nature22336)
- [OpenStax, population genetics](https://openstax.org/books/biology-2e/pages/19-2-population-genetics)
- [OpenStax, cardiac physiology](https://openstax.org/books/anatomy-and-physiology-2e/pages/19-4-cardiac-physiology)
- [3GPP TS 38.211, NR modulation and physical channels](https://www.etsi.org/deliver/etsi_ts/138200_138299/138211/17.09.00_60/ts_138211v170900p.pdf)
- [3GPP TS 38.331, A3 measurement event](https://www.etsi.org/deliver/etsi_ts/138300_138399/138331/18.06.00_60/ts_138331v180600p.pdf)
- [IETF RFC 5905, two-way delay and offset](https://datatracker.ietf.org/doc/html/rfc5905)

- [ITU-R P.525, free-space attenuation](https://www.itu.int/rec/R-REC-P.525-5-202411-I/en)
- [ITU-R P.838, rain attenuation](https://www.itu.int/rec/R-REC-P.838-3-200503-I)
- [3GPP TS 36.214, E-UTRA physical-layer measurements](https://www.etsi.org/deliver/etsi_ts/136200_136299/136214/18.00.00_60/ts_136214v180000p.pdf)
- [3GPP NR physical-layer specifications](https://portal.3gpp.org/Specifications.aspx?WiUid=750167&q=1)
- [OpenStax, University Physics](https://openstax.org/books/university-physics-volume-1/pages/1-introduction)
- [ITU-R P.526, Fresnel zones and diffraction](https://www.itu.int/rec/R-REC-P.526)
- [ITU-R terrestrial point-to-point planning handbook](https://www.itu.int/dms_pub/itu-r/opb/hdb/R-HDB-54-2009-OAS-PDF-E.pdf)
- [MathWorks, uncoded AWGN symbol-error rates](https://www.mathworks.com/help/comm/ref/berawgn.html)
- [NASA, cosmic distances](https://science.nasa.gov/solar-system/cosmic-distances/)

The telecom presets are examples with editable inputs. The link model includes user-entered rain attenuation. The Fresnel profile uses one obstacle and an effective Earth radius. QAM assumes normalized square constellations and AWGN. The satellite latency lab computes an idealized four-leg propagation time plus user-entered overhead.
