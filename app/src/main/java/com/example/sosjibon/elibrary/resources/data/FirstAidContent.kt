package com.example.sosjibon.elibrary.resources.data

import com.example.sosjibon.R

object FirstAidContent {

    private val ph = R.drawable.ic_placeholder

    val allConditions: List<EmergencyCondition> = listOf(

        // ========================= FIRST AID =========================
        EmergencyCondition(
            id = "bleeding", title = "Severe Bleeding", titleBn = "রক্তক্ষরণ",
            category = GuideCategory.FIRST_AID, cardIconRes = ph, severity = SeverityTag.RED,
            isMostUrgent = true,
            whatHappened = "Heavy or uncontrolled blood loss from a wound.",
            recognizeSigns = listOf("Blood soaking through cloth quickly", "Pale, clammy skin", "Dizziness or fainting"),
            doThis = listOf(
                GuideStep(1, "Call emergency services first if bleeding is severe.", ph, "Call for help"),
                GuideStep(2, "Apply firm, direct pressure with a clean cloth or sterile bandage.", ph, "Direct pressure"),
                GuideStep(3, "Keep firm pressure on the wound. Do not remove soaked cloths — add more on top.", ph, "Add cloths on top"),
                GuideStep(4, "Elevate the injured limb above heart level if no broken bones are suspected.", ph, "Elevate limb")
            ),
            dontDoThis = listOf(
                "Do not remove embedded objects from the wound.",
                "Do not remove blood-soaked cloths — add more layers instead.",
                "Do not use a tourniquet unless trained and as a last resort for life-threatening bleeding."
            ),
            whenToGetHelp = "Call emergency services immediately if blood spurts, won't stop after 10 minutes of direct pressure, or the person becomes pale or unconscious.",
            searchTags = listOf("cut", "wound", "blood", "hemorrhage", "artery"),
            relatedConditionIds = listOf("shock")
        ),

        EmergencyCondition(
            id = "burns", title = "Burns & Scalds", titleBn = "পোড়া ক্ষত",
            category = GuideCategory.FIRST_AID, cardIconRes = ph, severity = SeverityTag.BLUE,
            isMostUrgent = false,
            whatHappened = "Skin damage from heat, hot liquids, chemicals, or electricity.",
            recognizeSigns = listOf("Red, painful skin", "Blisters forming", "Charred or white skin in severe cases"),
            doThis = listOf(
                GuideStep(1, "Cool the burn immediately under cool running water for 10-20 minutes.", ph, "Cool under water"),
                GuideStep(2, "Remove clothing near the burn, but NOT if stuck to the burned skin.", ph, "Remove non-stuck clothing"),
                GuideStep(3, "Cover the burn loosely with clean cling film or a sterile non-stick bandage.", ph, "Cover loosely")
            ),
            dontDoThis = listOf(
                "Do not apply ice, ice water, butter, oil, or toothpaste to burns.",
                "Do not pop blisters.",
                "Do not remove clothing stuck to the burn."
            ),
            whenToGetHelp = "Get medical help if the burn is larger than the person's palm, on the face, hands, or groin, or if caused by chemicals or electricity.",
            searchTags = listOf("fire", "scald", "heat", "blister", "chemical burn"),
            relatedConditionIds = listOf("electric_shock")
        ),

        EmergencyCondition(
            id = "choking", title = "Choking (Adult/Child)", titleBn = "গলায় কিছু আটকানো",
            category = GuideCategory.FIRST_AID, cardIconRes = ph, severity = SeverityTag.RED,
            isMostUrgent = true,
            whatHappened = "An object blocks the airway, preventing breathing and speech.",
            recognizeSigns = listOf("Inability to speak or cough", "Clutching the throat", "Blueish lips or face"),
            doThis = listOf(
                GuideStep(1, "Encourage coughing if the person can cough or speak.", ph, "Encourage coughing"),
                GuideStep(2, "Give up to 5 back blows between shoulder blades with the heel of your hand.", ph, "5 back blows"),
                GuideStep(3, "Give up to 5 abdominal thrusts (Heimlich maneuver) if back blows fail.", ph, "5 abdominal thrusts"),
                GuideStep(4, "Repeat 5 back blows and 5 abdominal thrusts until object clears or person collapses.", ph, "Repeat cycle")
            ),
            dontDoThis = listOf(
                "Do not perform abdominal thrusts on infants under 1 year old.",
                "Do not attempt a blind finger sweep inside the mouth."
            ),
            whenToGetHelp = "Call emergency services if the object doesn't clear after 1 cycle or if the person loses consciousness.",
            searchTags = listOf("swallowed", "blockage", "cough", "suffocate", "heimlich"),
            relatedConditionIds = listOf("unresponsive_not_breathing")
        ),

        EmergencyCondition(
            id = "fractures", title = "Broken Bones & Fractures", titleBn = "হাড় ভাঙা",
            category = GuideCategory.ACCIDENTS, cardIconRes = ph, severity = SeverityTag.BLUE,
            isMostUrgent = false,
            whatHappened = "A crack or break in a bone caused by trauma or impact.",
            recognizeSigns = listOf("Severe pain & swelling", "Deformed or misaligned limb", "Inability to move the limb"),
            doThis = listOf(
                GuideStep(1, "Keep the injured area completely still and immobilized.", ph, "Immobilize limb"),
                GuideStep(2, "Support the injury using rolled towels, cushions, or a sling if available.", ph, "Support limb"),
                GuideStep(3, "Apply an ice pack wrapped in a cloth to reduce swelling (15 min on/off).", ph, "Apply ice pack")
            ),
            dontDoThis = listOf(
                "Do not try to realign or push back protruding bones.",
                "Do not move the person if a spinal or neck fracture is suspected."
            ),
            whenToGetHelp = "Call emergency services if bone pierces the skin (open fracture), or if neck/spine injury is suspected.",
            searchTags = listOf("bone", "break", "limb", "fall", "sprain"),
            relatedConditionIds = listOf("spinal_injury")
        ),

        // ========================= BREATHING & CONSCIOUSNESS =========================
        EmergencyCondition(
            id = "cardiac_emergency", title = "Cardiac Emergency / CPR", titleBn = "হৃদরোগ সম্পর্কিত জরুরি অবস্থা",
            category = GuideCategory.BREATHING_CONSCIOUSNESS, cardIconRes = ph, severity = SeverityTag.RED,
            isMostUrgent = true,
            whatHappened = "The heart stops pumping blood effectively, causing collapse and loss of breathing.",
            recognizeSigns = listOf("Unresponsive person", "Not breathing or only gasping", "No pulse"),
            doThis = listOf(
                GuideStep(1, "Call emergency services immediately and get an AED if available.", ph, "Call 999/AED"),
                GuideStep(2, "Place hands in center of chest and push hard & fast (100-120 beats/min).", ph, "Chest compressions"),
                GuideStep(3, "Give 30 compressions followed by 2 rescue breaths if trained.", ph, "30:2 ratio"),
                GuideStep(4, "Continue CPR until emergency help arrives or AED instructs otherwise.", ph, "Continue until help")
            ),
            dontDoThis = listOf(
                "Do not delay starting compressions to check pulse for too long.",
                "Do not stop CPR unless person shows clear signs of life or professional help takes over."
            ),
            whenToGetHelp = "Call emergency services immediately on collapse. Every minute without CPR reduces survival by 10%.",
            searchTags = listOf("heart attack", "cpr", "cardiac arrest", "chest pain", "pulse"),
            relatedConditionIds = listOf("unresponsive_not_breathing")
        ),

        EmergencyCondition(
            id = "unresponsive_breathing", title = "Unresponsive but Breathing", titleBn = "অজ্ঞান কিন্তু শ্বাসচলমান",
            category = GuideCategory.BREATHING_CONSCIOUSNESS, cardIconRes = ph, severity = SeverityTag.RED,
            isMostUrgent = true,
            whatHappened = "Person is unconscious but breathing normally.",
            recognizeSigns = listOf("No response to voice/touch", "Regular breathing detected"),
            doThis = listOf(
                GuideStep(1, "Place the person in the recovery position (on their side).", ph, "Recovery position"),
                GuideStep(2, "Tilt head back slightly to keep airway open.", ph, "Open airway"),
                GuideStep(3, "Call emergency services and continuously monitor breathing.", ph, "Monitor breathing")
            ),
            dontDoThis = listOf(
                "Do not leave the person on their back — vomit can block the airway.",
                "Do not give food or drink to an unconscious person."
            ),
            whenToGetHelp = "Call emergency services immediately whenever someone is unconscious.",
            searchTags = listOf("unconscious", "faint", "passed out", "recovery position"),
            relatedConditionIds = listOf("cardiac_emergency")
        ),

        EmergencyCondition(
            id = "unresponsive_not_breathing", title = "Unresponsive & Not Breathing", titleBn = "অজ্ঞান ও শ্বাস বন্ধ",
            category = GuideCategory.BREATHING_CONSCIOUSNESS, cardIconRes = ph, severity = SeverityTag.RED,
            isMostUrgent = true,
            whatHappened = "Person is unconscious and not breathing or only gasping.",
            recognizeSigns = listOf("Unresponsive", "No chest movement", "Gasping sounds"),
            doThis = listOf(
                GuideStep(1, "Call emergency services immediately.", ph, "Call emergency"),
                GuideStep(2, "Start CPR with continuous chest compressions.", ph, "Start CPR"),
                GuideStep(3, "Use AED as soon as available.", ph, "Use AED")
            ),
            dontDoThis = listOf(
                "Do not wait for paramedics before starting CPR."
            ),
            whenToGetHelp = "Call emergency services instantly.",
            searchTags = listOf("cpr", "cardiac arrest", "not breathing"),
            relatedConditionIds = listOf("cardiac_emergency")
        ),

        EmergencyCondition(
            id = "asthma_attack", title = "Asthma Attack", titleBn = "অ্যাজমা অ্যাটাক",
            category = GuideCategory.BREATHING_CONSCIOUSNESS, cardIconRes = ph, severity = SeverityTag.RED,
            isMostUrgent = false,
            whatHappened = "Airways narrow, causing severe shortness of breath and wheezing.",
            recognizeSigns = listOf("Wheezing sound", "Difficulty talking", "Hunched over breathing hard"),
            doThis = listOf(
                GuideStep(1, "Sit the person upright comfortably. Do not let them lie down.", ph, "Sit upright"),
                GuideStep(2, "Help them take 1-2 puffs of their blue reliever inhaler (with spacer if available).", ph, "Use inhaler"),
                GuideStep(3, "Give 1 puff every minute up to 4-10 puffs if symptoms do not improve.", ph, "Repeat inhaler")
            ),
            dontDoThis = listOf(
                "Do not force the person to lie down."
            ),
            whenToGetHelp = "Call emergency services if symptoms worsen or don't improve after 10 puffs.",
            searchTags = listOf("inhaler", "breath", "wheeze", "lungs"),
            relatedConditionIds = listOf("anaphylaxis")
        ),

        // ========================= MEDICAL CONDITIONS =========================
        EmergencyCondition(
            id = "stroke", title = "Stroke (FAST Test)", titleBn = "স্ট্রোক",
            category = GuideCategory.MEDICAL_CONDITIONS, cardIconRes = ph, severity = SeverityTag.RED,
            isMostUrgent = true,
            whatHappened = "Blood flow to part of the brain is interrupted.",
            recognizeSigns = listOf("Face drooping on one side", "Arm weakness (can't raise both)", "Slurred speech"),
            doThis = listOf(
                GuideStep(1, "Perform FAST check: Face drooping? Arm weakness? Speech slurred? Time to call emergency!", ph, "FAST check"),
                GuideStep(2, "Call emergency services immediately.", ph, "Call emergency"),
                GuideStep(3, "Keep person comfortable and supported until help arrives.", ph, "Support person")
            ),
            dontDoThis = listOf(
                "Do not give food, drink, or aspirin."
            ),
            whenToGetHelp = "Call emergency services immediately. Time is critical for stroke treatment.",
            searchTags = listOf("brain", "paralysis", "fast", "speech"),
            relatedConditionIds = listOf("cardiac_emergency")
        ),

        EmergencyCondition(
            id = "seizure", title = "Seizure / Epilepsy", titleBn = "খিঁচুনি / মৃগীরোগ",
            category = GuideCategory.MEDICAL_CONDITIONS, cardIconRes = ph, severity = SeverityTag.BLUE,
            isMostUrgent = false,
            whatHappened = "Sudden, uncontrolled electrical activity in the brain.",
            recognizeSigns = listOf("Jerking movements", "Loss of consciousness", "Drooling or foaming"),
            doThis = listOf(
                GuideStep(1, "Cushion their head and clear surrounding sharp objects.", ph, "Cushion head"),
                GuideStep(2, "Time the duration of the seizure.", ph, "Time seizure"),
                GuideStep(3, "Turn person onto side after jerking stops (recovery position).", ph, "Recovery position")
            ),
            dontDoThis = listOf(
                "Do not restrain the person.",
                "Do not put anything in their mouth."
            ),
            whenToGetHelp = "Call emergency services if seizure lasts > 5 minutes or person is injured.",
            searchTags = listOf("epilepsy", "convulsion", "jerking", "faint"),
            relatedConditionIds = listOf("unresponsive_breathing")
        ),

        EmergencyCondition(
            id = "diabetes_emergency", title = "Diabetic Emergency", titleBn = "ডায়াবেটিস জরুরি অবস্থা",
            category = GuideCategory.MEDICAL_CONDITIONS, cardIconRes = ph, severity = SeverityTag.BLUE,
            isMostUrgent = false,
            whatHappened = "Blood sugar drops too low (hypoglycemia) or rises too high.",
            recognizeSigns = listOf("Sweating & shakiness", "Confusion or irritability", "Dizziness"),
            doThis = listOf(
                GuideStep(1, "If conscious, give 15-20g of fast-acting sugar (fruit juice, candy, sugar drink).", ph, "Give sugar"),
                GuideStep(2, "Wait 15 minutes and check if person feels better.", ph, "Wait 15 mins"),
                GuideStep(3, "Follow with a meal or snack (sandwich/biscuit).", ph, "Give snack")
            ),
            dontDoThis = listOf(
                "Do not give food or drink if person is unconscious or unable to swallow."
            ),
            whenToGetHelp = "Call emergency services if sugar doesn't help or person loses consciousness.",
            searchTags = listOf("sugar", "hypo", "insulin", "faint"),
            relatedConditionIds = listOf("unresponsive_breathing")
        ),

        // ========================= POISON & BITES =========================
        EmergencyCondition(
            id = "poisoning", title = "Poisoning / Ingestion", titleBn = "বিষাক্ত পদার্থ সেবন",
            category = GuideCategory.POISON_BITES, cardIconRes = ph, severity = SeverityTag.RED,
            isMostUrgent = true,
            whatHappened = "Swallowing or inhaling toxic chemicals, medicines, or household cleaners.",
            recognizeSigns = listOf("Nausea or vomiting", "Burns around mouth", "Drowsiness or confusion"),
            doThis = listOf(
                GuideStep(1, "Call emergency poison control or emergency hotline immediately.", ph, "Call poison hotline"),
                GuideStep(2, "Keep the container or packaging of the suspected substance.", ph, "Save container"),
                GuideStep(3, "Rinse mouth with water if substance was swallowed and person is conscious.", ph, "Rinse mouth")
            ),
            dontDoThis = listOf(
                "Do not induce vomiting unless specifically instructed by medical professionals.",
                "Do not give milk or salt water."
            ),
            whenToGetHelp = "Call emergency services immediately for any chemical or unknown substance ingestion.",
            searchTags = listOf("toxic", "chemical", "swallow", "overdose"),
            relatedConditionIds = listOf("snake_bites")
        ),

        EmergencyCondition(
            id = "snake_bites", title = "Snake Bite", titleBn = "সাপের কামড়",
            category = GuideCategory.POISON_BITES, cardIconRes = ph, severity = SeverityTag.RED,
            isMostUrgent = true,
            whatHappened = "Bite from venomous or non-venomous snake.",
            recognizeSigns = listOf("Two puncture marks", "Rapid swelling & pain", "Nausea or difficulty breathing"),
            doThis = listOf(
                GuideStep(1, "Keep victim calm and still. Movement spreads venom faster.", ph, "Keep still"),
                GuideStep(2, "Immobilize the bitten limb at or below heart level.", ph, "Immobilize limb"),
                GuideStep(3, "Remove jewelry, rings, or tight clothing near the bite area.", ph, "Remove jewelry"),
                GuideStep(4, "Transport urgently to nearest hospital with anti-venom.", ph, "Transport to hospital")
            ),
            dontDoThis = listOf(
                "Do not cut the wound or try to suck out venom.",
                "Do not apply tourniquets or ice packs.",
                "Do not try to capture the snake."
            ),
            whenToGetHelp = "Go to emergency room immediately.",
            searchTags = listOf("snake", "venom", "bite", "reptile"),
            relatedConditionIds = listOf("poisoning", "insect_bites")
        ),

        EmergencyCondition(
            id = "insect_bites", title = "Insect Bites & Stings", titleBn = "পোকা ও মৌমাছির হুল",
            category = GuideCategory.POISON_BITES, cardIconRes = ph, severity = SeverityTag.GREEN,
            isMostUrgent = false,
            whatHappened = "Bee, wasp, or insect sting causing localized pain or allergic reaction.",
            recognizeSigns = listOf("Local redness & swelling", "Itagging or pain", "Stinger visible in skin"),
            doThis = listOf(
                GuideStep(1, "Scrape out the stinger using a credit card edge. Do not squeeze with tweezers.", ph, "Scrape stinger"),
                GuideStep(2, "Wash area with soap and water.", ph, "Wash area"),
                GuideStep(3, "Apply cold compress to reduce swelling.", ph, "Cold compress")
            ),
            dontDoThis = listOf(
                "Do not squeeze the stinger with tweezers — it injects more venom."
            ),
            whenToGetHelp = "Call emergency services if person develops hives, facial swelling, or breathing trouble (anaphylaxis).",
            searchTags = listOf("bee", "wasp", "sting", "allergy"),
            relatedConditionIds = listOf("anaphylaxis")
        ),

        EmergencyCondition(
            id = "anaphylaxis", title = "Anaphylaxis (Severe Allergy)", titleBn = "অ্যালার্জি প্রতিক্রিয়া (অ্যানাফিল্যাক্সিস)",
            category = GuideCategory.POISON_BITES, cardIconRes = ph, severity = SeverityTag.RED,
            isMostUrgent = true,
            whatHappened = "Life-threatening allergic reaction causing airway constriction.",
            recognizeSigns = listOf("Swollen lips/throat", "Difficulty breathing", "Hives or rash across body"),
            doThis = listOf(
                GuideStep(1, "Administer EpiPen (epinephrine auto-injector) into outer thigh if available.", ph, "Use EpiPen"),
                GuideStep(2, "Call emergency services immediately.", ph, "Call emergency"),
                GuideStep(3, "Keep person lying flat with legs raised (unless breathing is easier sitting up).", ph, "Lay flat")
            ),
            dontDoThis = listOf(
                "Do not delay giving epinephrine if available."
            ),
            whenToGetHelp = "Call emergency services immediately.",
            searchTags = listOf("epipen", "allergy", "anaphylaxis", "swelling"),
            relatedConditionIds = listOf("asthma_attack")
        ),

        // ========================= ACCIDENTS =========================
        EmergencyCondition(
            id = "drowning", title = "Drowning & Submersion", titleBn = "পানিতে ডোবা",
            category = GuideCategory.ACCIDENTS, cardIconRes = ph, severity = SeverityTag.RED,
            isMostUrgent = true,
            whatHappened = "Respiratory impairment from submersion in liquid.",
            recognizeSigns = listOf("Coughing up foam", "Unconscious", "Not breathing normally"),
            doThis = listOf(
                GuideStep(1, "Remove victim safely from water without endangering yourself.", ph, "Rescue safely"),
                GuideStep(2, "Check for breathing and start CPR immediately if not breathing.", ph, "Start CPR"),
                GuideStep(3, "Give 5 rescue breaths first before starting chest compressions for drowning.", ph, "5 rescue breaths")
            ),
            dontDoThis = listOf(
                "Do not attempt water rescue if you cannot swim safely."
            ),
            whenToGetHelp = "Call emergency services immediately.",
            searchTags = listOf("water", "drown", "pool", "river"),
            relatedConditionIds = listOf("cardiac_emergency")
        ),

        EmergencyCondition(
            id = "electric_shock", title = "Electrical Shock", titleBn = "বিদ্যুৎপৃষ্ট হওয়া",
            category = GuideCategory.ACCIDENTS, cardIconRes = ph, severity = SeverityTag.RED,
            isMostUrgent = true,
            whatHappened = "Electrical current passing through the body.",
            recognizeSigns = listOf("Burn marks at entry/exit points", "Unconsciousness", "Irregular heart rhythm"),
            doThis = listOf(
                GuideStep(1, "Turn off main power source BEFORE touching the victim.", ph, "Turn off power"),
                GuideStep(2, "If power cannot be turned off, use a non-conductive object (wooden broom) to push victim free.", ph, "Use wooden stick"),
                GuideStep(3, "Check breathing and begin CPR if unresponsive.", ph, "Check breathing")
            ),
            dontDoThis = listOf(
                "Do not touch the victim while they are still in contact with electrical current."
            ),
            whenToGetHelp = "Call emergency services immediately for high voltage shock.",
            searchTags = listOf("power", "electric", "current", "wire"),
            relatedConditionIds = listOf("cardiac_emergency", "burns")
        )
    )

    val mostUrgent: List<EmergencyCondition> = allConditions.filter { it.isMostUrgent }

    fun getById(id: String): EmergencyCondition? = allConditions.find { it.id == id }
}
