# MARS-OPS — تقرير المراجعة الفنية الشامل

فُحصت جميع ملفات Java الـ45 في Prog--MiniProject/src وملف PDF الوحيد. لم يوجد UML أو PDF متطلبات أصلي؛ لذلك لا أدعي مطابقة غير قابلة للتحقق. لم تعدل ملفات المصدر.

## المحتويات
1. البنية والحزم
2. شرح سلوك المجال
3. OOP والمجموعات
4. مقارنة الدليل
5. GUI
6. المخاطر
7. أسئلة مراجعة (45)
8. الاختبارات وجرد الملفات

## 1. البنية والحزم

تطبيق Java 17 لمحاكاة مستعمرة مريخ. Main.main يشغّل خمس حالات نصية. gui.Main ينشئ Colony تجريبية وAppSession وAdminDashboard. admingui.AdminGuiMain يشغّل AdminConsole المستقلة عن الواجهة الأولى. لا يوجد persistence ظاهر.

| الحزمة | المسؤولية |
|---|---|
| agents | Agent interface |
| personnel | Personnel abstract وEngineer وScientist وMedicalOfficer |
| roboticunit | RoboticUnit abstract وDrone وExplorationRover وRepairRobot |
| missions | Mission abstract وExploration وResearch وRescue وRepair |
| colony | تسجيل العناصر، قبول المهام، إدارة الموارد والطوارئ |
| facilities | Facilities abstract، أنواع المرافق، Equipment وLifeSupportSystem |
| emergencies | Emergency abstract وFire وMedicalEmergency وOxygenLeak وRoverBreakdown |
| enums | أنواع الموارد والقدرات والمهارات والحالات |
| marsenvironment | قيود البيئة والاتصالات |
| simulation | تقدم الزمن |
| gui | AdminDashboard وAppSession وScenarioLabPanel |
| admingui | AdminConsole واللوحات ومشهد المستعمرة |
| tests | MissionAssignmentTest |
| resources | enum موارد مكرر الاسم، غير مستخدم في المجال |

### جرد مسؤوليات كل صنف
| الأصناف | الحقول/المسؤولية والطرق الأساسية |
|---|---|
| Colony | قوائم personnel/facilities/robots/missions/emergencies وactiveMission والبيئة؛ add، submitMission، prepareMissionStart، start/finish/fail، advanceTimeUnit، handleEmergency، getters |
| Mission | agents، duration/elapsed، facility/state/resources/capabilities/failureReason/colony/assigningAgent؛ assign/unassign/start/advance/finish/fail/reject/checkresources |
| Exploration | region؛ setters/getters وhasValidTeamComposition |
| Research | topic وexperimentCompleted؛ شرط المختبر/الفريق وhook نجاح |
| Rescue | شرط الفريق؛ constructor |
| Repair | repairObject؛ setObject/getObject/performRepair/hook نجاح/شرط الفريق |
| Personnel | name/id/health/location/skills/fatigue/availability/mission؛ validation/getters/setters/assign/canPerform/emergency |
| Engineer/Scientist/MedicalOfficer | تخصص canPerform؛ MedicalOfficer يضيف overload للطوارئ |
| RoboticUnit | battery/health/location/availability/capabilities/mission/equipment؛ canPerform والبطارية والتعيين |
| Drone/Rover/RepairRobot | constructors وقدرات الروبوتات؛ rover يأخذ القدرات، والآخران يثبتانها |
| Facilities | type/location/capacity/status/resources/equipment/occupants؛ validation ونسخ getters وresource APIs |
| Habitat | LifeSupportSystem؛ applyOxygenLoss |
| Laboratory | onFire؛ catchFire/extinguishFire/repair |
| StorageFacility | getTotalStored/store |
| PowerStation | outputPerTimeUnit؛ generatePower |
| CommunicationStation | isCommunicationAvailable |
| Equipment | name/status/category GENERAL أو SCIENTIFIC؛ constructors/getters/setters |
| Emergency | type/status/reasons/location/agents/required resources/occurrence flag؛ register/responders/mark states، applyResponse abstract |
| OxygenLeak | affected facility/loss/repaired؛ occurrence/unresolved/response |
| Fire | affected facility؛ يفرض Laboratory |
| MedicalEmergency | patient؛ يتطلب facility ويجعل المريض Stable |
| RoverBreakdown | rover؛ occurrence يعطل ويفشل mission |
| MarsEnvironment | environmentState؛ قيود بدء/فشل mission |
| Simulation | static time؛ advance/reset/show |
| gui.Main | بدء Dashboard وإنشاء demo colony |
| AppSession | colony/environment/drafts/activity داخل الذاكرة؛ record وdraft APIs |
| AdminDashboard | Swing dashboard، navigation، CRUD، missions/emergencies، refresh/error handling |
| ScenarioLabPanel | تشغيل سيناريوهات GUI وFixtures |
| AdminConsole | JFrame للواجهة الثانية وتركيب اللوحات |
| AdminGuiMain | نقطة تشغيل الواجهة الثانية |
| ColonyAnimationPanel | paintComponent ورسوم المباني/الأفراد/المهمات والطقس |
| DashboardPanel | عرض موارد وأزرار تقدم الوقت |
| PersonnelPanel/RoboticUnitsPanel | إدارة الأفراد/الروبوتات |
| FacilitiesPanel | إدارة facilities/equipment/occupants |
| MissionsPanel | إنشاء الفريق، الإرسال، البدء، الإتمام والفشل |
| EmergenciesPanel | إنشاء/استجابة emergency |
| Refreshable | interface refresh |
| SampleColonyFactory | إنشاء عينة للمسار الثاني |
| SimulationContext | سياق مشترك للـpanels |
| Theme | تنسيق Swing |
| MissionAssignmentTest | TestAgent داخلي وأربع اختبارات يدوية |

## 2. سلوك المجال

### تدفق submitMission
Colony.submitMission يرفض null أو mission مكررة أو مرتبطة بغير Colony أو ذات state سابق. يتحقق من وجود فريق، slot النشط، التكرار، التوفر، agent.canPerform، تركيب الفريق، البيئة، والموارد. بعدها يربط Colony، يسجل المهمة ويجعلها active قبل الإسناد. إن وقع RuntimeException، يفك ما سبق ويزيل active ثم يسجل الرفض. بعض حالات الرفض المبكرة ترجع false دون أن تسجل المهمة أو تجعلها FAILED.

Mission.assign يتحقق ثانيةً ثم يضع assigningAgent وينادي Agent.assignMission. تطبيق الوكيل يعود إلى mission.assign عند عدم وجود الحارس؛ assigningAgent يمنع recursion. في الفشل يحاول تحرير الوكيل. Colony يكمل rollback للفريق السابق.

### حالات المهمة
| الحالة | العملية/الشرط | التالية |
|---|---|---|
| null (Draft) | إرسال وإسناد مقبول | ASSIGNED |
| null | rejectMission | FAILED |
| ASSIGNED | start مع ارتباط وصلاحية وموارد وبطارية | INPROGRESS |
| ASSIGNED | فشل start | FAILED |
| ASSIGNED | unassign | ASSIGNED أو null لفريق فارغ أو FAILED لتكوين غير صالح |
| INPROGRESS | تقدم الزمن | INPROGRESS أو COMPLETED عند المدة |
| INPROGRESS | فشل بيئي/بطارية/صريح | FAILED |
| INPROGRESS | finish(true/false) | COMPLETED/FAILED |

finish يتطلب INPROGRESS ويرمي IllegalStateException خلاف ذلك. terminal transitions تحرر الوكلاء وتمسح قائمة الفريق وتزيل active mission.

prepareMissionStart يخطط استهلاك الموارد عبر المرافق التشغيلية، ويتأكد من إجمالي الكفاية قبل الخصم؛ snapshots وإرجاعها تحمي من خصم جزئي. IdentityHashMap تمنع معالجة المرجع ذاته مرتين. resourcesPreparedMissions يمنع الخصم مرتين.

Simulation.advanceTime يرفض null أو مدة <=0، ثم يزيد الوقت ويستدعي Colony.advanceTimeUnit. Colony يفحص البيئة قبل تقدم Mission. البطارية الروبوتية تنقص 1 لكل وحدة؛ completion آلي عند بلوغ المدة. PowerStation.generatePower غير موصولة بدورة Simulation.

### أنواع الوكلاء والمهمات والمرافق
Personnel.canPerform الأساسي يتحقق من التوفر والمهمة غير null. Engineer يسمح Repair ميكانيكي أو Rescue communication؛ Scientist يسمح Research/Exploration علمي أو Rescue communication؛ MedicalOfficer يسمح Rescue فقط في overload الخاص بـMission. RoboticUnit يشترط قدرات المهمة والبطارية.

Exploration يتطلب عالماً علمياً وروبوتاً بقدرات surface travel/equipment carrying ومعدة تشغيلية. EXPLORATION capability ليست شرطاً إلا إذا وضعتها المهمة في requiredCapabilities. Research يتطلب Laboratory وعالماً ومعدة scientific operational؛ نجاحه يضبط experimentCompleted. Rescue يتطلب communication ووكيل surface/access. Repair يتطلب هدف DAMAGED وفريقاً فيه MECHANICAL وEQUIPMENT_REPAIR؛ النجاح يعيد الهدف OPERATIONAL.

Facilities تنسخ Maps/Lists سطحياً. Colony يحسب مخزون المرافق OPERATIONAL. deductResource يخصم حتى المتاح ويرجع المقدار. StorageFacility يفحص capacity. Laboratory بعد الإطفاء DAMAGED. Habitat يخفض oxygen مباشرة؛ LifeSupportSystem لا يدخل في الحساب. Equipment مشتركة mutable.

Colony.handleEmergency يسجل occurrence مرة، يجمع كل المستجيبين المؤهلين، يفحص العدد والموقع التشغيلي والموارد، يخصم، ثم ينقل المستجيبين ويشغل applyResponse، وfinally يحررهم. OxygenLeak ينقص oxygen عند occurrence ويتطلب ميكانيكياً بشرياً وروبوتاً وspare part؛ onUnresolved ينقص oxygen مرة أخرى. Fire يفرض Laboratory وتصبح DAMAGED بعد الإطفاء. MedicalEmergency يستهلك medical supplies ويجعل صحة المريض Stable؛ تغيير المريض لا يغير الموقع المحفوظ. RoverBreakdown يفشل المهمة ويجعل الروفر DAMAGED وغير متاح؛ applyResponse لا يصلحه.

## 3. OOP والمجموعات

Encapsulation عبر private والـvalidation؛ نسخ collections دفاعية لكنها سطحية. Personnel وRoboticUnit يطبقان Agent، والتخصصات ترث منهما. Mission/Facilities/Emergency مجردة؛ Emergency تفرض applyResponse، بينما Mission لا تفرض abstract method. Polymorphism في agent.canPerform وEmergency.applyResponse وMission.onSuccessfulCompletion. Habitat ينشئ LifeSupportSystem تركيباً؛ Colony يجمع مراجع المجال.

List تسمح بالتكرار وتحفظ الترتيب؛ Colony يمنع تكرار Facility فقط. Set للمهارات/القدرات وتكرار الفريق؛ Map لمقادير الموارد؛ EnumSet للقدرات المعروفة. قيم Enums:
- Capability: SURFACE_TRAVEL, EQUIPMENT_CARRYING, EXPLORATION, EQUIPMENT_REPAIR, FACILITY_OPERATIONS, AERIAL_RECONNAISSANCE, ACCESS_INACCESSIBLE_AREAS.
- Skills: MECHANICAL, SCIENTIFIC, MEDICAL, COMMUNICATION, FIRE_RESPONSE.
- Resources: OXYGEN, WATER, FOOD, FUEL, BATTERY_POWER, MEDICAL_SUPPLIES, SPARE_PARTS.
- OperationalStatus: OPERATIONAL, DAMAGED, UNAVAILABLE, DESTROYED.
- FacilityType: HABITAT, LABORATORY, STORAGE, POWER_STATION, COMMUNICATION_STATION.
- Mission_Emergency_States: ASSIGNED, COMPLETED, FAILED, INPROGRESS.
- EmergencyType: OXYGEN_LEAK, FIRE, ROVER_BREAKDOWN, MEDICAL.
- EmergencyStatus: PENDING, RESOLVED, UNRESOLVED.
- EnviromentStates: DUST_STORM, COMMUNICATION_DISTRIBUTION, NORMAL_CONDITION.
- Equipment.Category: GENERAL, SCIENTIFIC.
- resources.Resources يكرر القيم لكنه enum مختلف.

## 4. مقارنة PDF

PDF المتاح هو Test Playbook: 19 حالات GUI وخمسة walkthroughs. لا UML أو requirements PDF أصلي.

| التوقع | النتيجة | الحالة والمرجع |
|---|---|---|
| Exploration صالح ورفض الفريق/الموارد | موجود | Implemented — Colony.submitMission وExploration |
| مهمة فعالة واحدة | موجود | Implemented — Colony |
| Rescue communication + مركبة | موجود | Implemented — Rescue |
| العاصفة تمنع Exploration | موجود | Implemented — MarsEnvironment |
| guards للبدء/الإتمام وautocompletion | موجود | Implemented — Mission |
| OxygenLeak الافتراضي ينقص 1 وspare part | موجود عند constructor الافتراضي | Implemented — OxygenLeak/Colony |
| Fire يتطلب Laboratory | موجود | Implemented — Fire |
| NaN resource ومدة صفر يرفضان | موجود | Implemented — Facilities/Simulation |
| حفظ البيانات عند الإغلاق | غير موجود؛ AppSession ذاكرة | Not Implemented |
| UML والمواصفات الأصلية | غير متاحة | Cannot Verify |

## 5. الواجهات

gui.Main يبدأ Swing على Event Dispatch Thread. AppSession يحمل Colony والبيئة والمسودات وسجل نشاط بذاكرة الجلسة. AdminDashboard (2801 سطر) يعرض الصفحات ويستدعي المجال مباشرة في إجراءات CRUD والمهمات والطوارئ، ويعالج العرض عبر reportFailure/showFeedback. ScenarioLabPanel يبني Fixtures لكل حالة ويعرض النتائج. لم تُفتح Swing تفاعلياً.

admingui.AdminConsole منفصلة وتضم DashboardPanel وPersonnelPanel وRoboticUnitsPanel وFacilitiesPanel وMissionsPanel وEmergenciesPanel، مع refresh عبر Refreshable. SampleColonyFactory يبني العينة؛ Theme للتنسيق؛ SimulationContext للحالة؛ ColonyAnimationPanel يرسم المشهد.

## 6. المخاطر

1. **High:** OxygenLeak يخصم Oxygen في onOccurrence ثم مرة ثانية في onUnresolved. عدم توافر المستجيبين يؤدي لنقص مزدوج.
2. **High:** RoverBreakdown.applyResponse ترجع نجاحاً دون إعادة الروفر إلى العمل.
3. **Medium:** emergency resources لا تسترد عند فشل applyResponse بعد الخصم.
4. **Medium:** MedicalEmergency.setPatient لا يحدث location.
5. **Medium:** setters تسمح بتغيير مدة/قدرات/منشأة Mission بعد assignment؛ requiredResources فقط تمنع التغيير.
6. **Medium:** إرسال جميع المستجيبين المؤهلين، لا الحد الأدنى.
7. **Low:** addPersonnel/addRobot يسمحان بتكرار المرجع.
8. **Low:** PowerStation لا تولد تلقائياً مع تقدم الوقت.
9. **Low:** enum Resources مكرر في resources وenums.
10. **Low:** LifeSupportSystem لا يشارك في حسابات.
11. **Low:** occupants لا تتزامن مع موقع/تعيين الوكلاء.
12. **Low:** السعة لا تفرض عموماً إلا في StorageFacility.store.

## 7. أسئلة مراجعة (45)

1 نقطة CLI؟ Main.main. 2 GUI الحديثة؟ gui.Main. 3 هل AdminConsole هي نفسها؟ لا. 4 دور Agent؟ عقد موحد. 5 من يطبقها؟ Personnel وRoboticUnit. 6 منع الوكيل غير المتاح؟ Colony وMission. 7 assigningAgent؟ حارس recursion. 8 ماذا عند exception إسناد؟ rollback. 9 مهمتان نشطتان؟ لا. 10 null state؟ Draft. 11 متى ASSIGNED؟ بعد الإسناد. 12 متى الموارد تخصم؟ عند البدء. 13 مصدر الموارد؟ facilities التشغيلية. 14 deductResource يرفض النقص؟ لا، يرجع الفعلي. 15 rollback؟ snapshots. 16 لماذا IdentityHashMap؟ هوية المرجع. 17 autocompletion؟ elapsed يبلغ needed. 18 استهلاك الروبوت؟ 1 بطارية/وحدة. 19 استهلاك Personnel؟ لا. 20 شرط Exploration؟ عالم وروبوت مجهز. 21 هل EXPLORATION مطلوبة دائماً؟ لا. 22 شرط Research؟ مختبر وعالم ومعدة علمية. 23 شرط Repair؟ ميكانيكي وقدرة repair. 24 هل شخص واحد يحقق الشرطين؟ نعم. 25 شرط Rescue؟ communication ومركبة. 26 منع العاصفة؟ MarsEnvironment. 27 عاصفة أثناء المهمة؟ Colony يفشلها قبل التقدم. 28 fail/reject؟ حالات سابقة مختلفة. 29 finish قبل البدء؟ exception. 30 تحرير الوكلاء؟ makeAvailable. 31 نسخ getters لماذا؟ حماية collection. 32 هل النسخ عميقة؟ لا. 33 Set لماذا؟ إزالة التكرار. 34 OxygenLeak؟ oxygen/spare part/مستجيبان. 35 سبب النقص المزدوج؟ occurrence/unresolved. 36 المختبر بعد الإطفاء؟ DAMAGED. 37 breakdown يصلح الروفر؟ لا. 38 PowerStation تلقائية؟ لا. 39 persistence؟ لا. 40 اختبار MissionAssignmentTest؟ rollback/lifecycle. 41 JUnit؟ لا. 42 enum مكرر؟ نوعان غير متوافقين. 43 protected hook؟ تخصيص subclass. 44 dynamic dispatch؟ canPerform حسب الصنف الفعلي. 45 UML match؟ غير قابل للتحقق.

## 8. العرض والاختبارات والجرد

عرض شفهي: الهدف، العمارة، الكيانات، OOP، العلاقات، قبول المهمة، الموارد والزمن، الطوارئ والبيئة، GUI، القيود.

MissionAssignmentTest فيه أربع حالات يدوية: rollback ونجاح lifecycle وفشل lifecycle وfail active mission. جمعت كل المصدر بـjavac -encoding UTF-8 إلى مجلد مؤقت خارج المشروع؛ الترميز الافتراضي فشل على emoji في AdminConsole، وUTF-8 نجح. كل الاختبارات الأربعة PASSED. Main نفذ السيناريوهات الخمسة؛ خسارة Oxygen في مثال الطوارئ 10→8 لأن loss=2، وspare parts 3→2. لم أشغل Swing تفاعلياً.

جرد Java كاملاً:
- root: Main.java
- agents: Agent.java
- colony: Colony.java
- emergencies: Emergency.java, Fire.java, MedicalEmergency.java, OxygenLeak.java, RoverBreakdown.java
- enums: Capability.java, EmergencyStatus.java, EmergencyType.java, EnviromentStates.java, FacilityType.java, Mission_Emergency_States.java, OperationalStatus.java, Resources.java, Skills.java
- facilities: CommunicationStation.java, Equipment.java, Facilities.java, Habitat.java, Laboratory.java, LifeSupportSystem.java, PowerStation.java, StorageFacility.java
- gui: AdminDashboard.java, AppSession.java, Main.java, ScenarioLabPanel.java
- admingui: AdminConsole.java, AdminGuiMain.java, ColonyAnimationPanel.java, DashboardPanel.java, EmergenciesPanel.java, FacilitiesPanel.java, MissionsPanel.java, PersonnelPanel.java, Refreshable.java, RoboticUnitsPanel.java, SampleColonyFactory.java, SimulationContext.java, Theme.java
- marsenvironment: MarsEnvironment.java
- missions: Exploration.java, Mission.java, Repair.java, Rescue.java, Research.java
- personnel: Engineer.java, MedicalOfficer.java, Personnel.java, Scientist.java
- resources: Resources.java
- roboticunit: Drone.java, ExplorationRover.java, RepairRobot.java, RoboticUnit.java
- simulation: Simulation.java
- tests: MissionAssignmentTest.java

لم يوجد AGENTS.md أو UML/requirements/build descriptor. لم تعدل ملفات المصدر.

