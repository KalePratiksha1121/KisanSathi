package com.example.kisansathi;

import android.content.Context;
import android.content.SharedPreferences;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.UUID;

public class ExpertCaseStorage {

    private static final String PREFS_NAME =
            "KisanSathiExpertCases";

    private static final String CASES_KEY =
            "expert_cases";

    private static final String FARMER_PREFS_NAME =
            "KisanSathiPrefs";

    private static final String MOBILE_KEY =
            "mobile_number";

    private ExpertCaseStorage() {
        // Prevent direct object creation.
    }

    // ============================================================
    // CREATE NEW CASE
    // ============================================================

    public static String createCase(
            Context context,
            String imageUri,
            String disease,
            float confidence,
            boolean unknownDisease
    ) {

        SharedPreferences preferences =
                context.getSharedPreferences(
                        PREFS_NAME,
                        Context.MODE_PRIVATE
                );

        String caseId =
                "KS-" +
                        UUID.randomUUID()
                                .toString()
                                .substring(0, 8)
                                .toUpperCase();

        SharedPreferences farmerPreferences =
                context.getSharedPreferences(
                        FARMER_PREFS_NAME,
                        Context.MODE_PRIVATE
                );

        String farmerName =
                farmerPreferences.getString(
                        "user_name",
                        "Farmer"
                );

        String mobile =
                farmerPreferences.getString(
                        "mobile_number",
                        ""
                );

        String village =
                farmerPreferences.getString(
                        "village",
                        "Not available"
                );

        String district =
                farmerPreferences.getString(
                        "district",
                        "Not available"
                );

        String priority =
                unknownDisease || confidence < 70.0f
                        ? "HIGH"
                        : "NORMAL";

        try {

            JSONArray cases =
                    new JSONArray(
                            preferences.getString(
                                    CASES_KEY,
                                    "[]"
                            )
                    );

            JSONObject newCase =
                    new JSONObject();

            newCase.put(
                    "case_id",
                    caseId
            );

            newCase.put(
                    "image_uri",
                    imageUri
            );

            newCase.put(
                    "ai_disease",
                    disease
            );

            newCase.put(
                    "ai_confidence",
                    confidence
            );

            newCase.put(
                    "unknown_disease",
                    unknownDisease
            );

            newCase.put(
                    "priority",
                    priority
            );

            // Case is waiting for expert review.
            newCase.put(
                    "status",
                    "PENDING"
            );

            newCase.put(
                    "farmer_name",
                    farmerName
            );

            newCase.put(
                    "mobile_number",
                    mobile
            );

            newCase.put(
                    "village",
                    village
            );

            newCase.put(
                    "district",
                    district
            );

            newCase.put(
                    "expert_disease",
                    ""
            );

            newCase.put(
                    "expert_remarks",
                    ""
            );

            newCase.put(
                    "created_at",
                    System.currentTimeMillis()
            );

            newCase.put(
                    "reviewed_at",
                    0
            );

            // False means the farmer has not viewed
            // the expert-reviewed result yet.
            newCase.put(
                    "viewed",
                    false
            );

            cases.put(newCase);

            preferences.edit()
                    .putString(
                            CASES_KEY,
                            cases.toString()
                    )
                    .apply();

            return caseId;

        } catch (Exception e) {

            e.printStackTrace();

            return null;
        }
    }

    // ============================================================
    // GET ALL CASES
    // ============================================================

    public static JSONArray getAllCases(
            Context context
    ) {

        SharedPreferences preferences =
                context.getSharedPreferences(
                        PREFS_NAME,
                        Context.MODE_PRIVATE
                );

        try {

            return new JSONArray(
                    preferences.getString(
                            CASES_KEY,
                            "[]"
                    )
            );

        } catch (Exception e) {

            e.printStackTrace();

            return new JSONArray();
        }
    }

    // ============================================================
    // GET CURRENT FARMER MOBILE
    // ============================================================

    private static String getCurrentFarmerMobile(
            Context context
    ) {

        SharedPreferences preferences =
                context.getSharedPreferences(
                        FARMER_PREFS_NAME,
                        Context.MODE_PRIVATE
                );

        return preferences.getString(
                MOBILE_KEY,
                ""
        ).trim();
    }

    // ============================================================
    // GET CASES FOR CURRENT FARMER
    // ============================================================

    public static JSONArray getCasesForCurrentFarmer(
            Context context
    ) {

        JSONArray allCases =
                getAllCases(context);

        JSONArray farmerCases =
                new JSONArray();

        String currentMobile =
                getCurrentFarmerMobile(context);

        if (currentMobile.isEmpty()) {

            return farmerCases;
        }

        for (
                int i = 0;
                i < allCases.length();
                i++
        ) {

            try {

                JSONObject currentCase =
                        allCases.getJSONObject(i);

                String caseMobile =
                        currentCase.optString(
                                MOBILE_KEY,
                                ""
                        ).trim();

                if (
                        currentMobile.equals(
                                caseMobile
                        )
                ) {

                    farmerCases.put(
                            currentCase
                    );
                }

            } catch (Exception e) {

                e.printStackTrace();
            }
        }

        return farmerCases;
    }

    // ============================================================
    // GET CASE BY ID
    // ============================================================

    public static JSONObject getCaseById(
            Context context,
            String caseId
    ) {

        if (
                caseId == null
                        || caseId.trim().isEmpty()
        ) {

            return null;
        }

        JSONArray cases =
                getAllCases(context);

        for (
                int i = 0;
                i < cases.length();
                i++
        ) {

            try {

                JSONObject currentCase =
                        cases.getJSONObject(i);

                String storedCaseId =
                        currentCase.optString(
                                "case_id",
                                ""
                        );

                if (
                        caseId.equals(
                                storedCaseId
                        )
                ) {

                    return currentCase;
                }

            } catch (Exception e) {

                e.printStackTrace();
            }
        }

        return null;
    }

    // ============================================================
    // GET CASE BY ID FOR CURRENT FARMER
    // ============================================================

    public static JSONObject getCurrentFarmerCaseById(
            Context context,
            String caseId
    ) {

        if (
                caseId == null
                        || caseId.trim().isEmpty()
        ) {

            return null;
        }

        JSONArray farmerCases =
                getCasesForCurrentFarmer(
                        context
                );

        for (
                int i = 0;
                i < farmerCases.length();
                i++
        ) {

            try {

                JSONObject currentCase =
                        farmerCases.getJSONObject(i);

                String storedCaseId =
                        currentCase.optString(
                                "case_id",
                                ""
                        );

                if (
                        caseId.equals(
                                storedCaseId
                        )
                ) {

                    return currentCase;
                }

            } catch (Exception e) {

                e.printStackTrace();
            }
        }

        return null;
    }

    // ============================================================
    // UPDATE EXPERT RESULT
    // ============================================================

    public static boolean updateExpertResult(
            Context context,
            String caseId,
            String expertDisease,
            String expertRemarks
    ) {

        SharedPreferences preferences =
                context.getSharedPreferences(
                        PREFS_NAME,
                        Context.MODE_PRIVATE
                );

        JSONArray cases =
                getAllCases(context);

        for (
                int i = 0;
                i < cases.length();
                i++
        ) {

            try {

                JSONObject currentCase =
                        cases.getJSONObject(i);

                String storedCaseId =
                        currentCase.optString(
                                "case_id",
                                ""
                        );

                if (
                        caseId.equals(
                                storedCaseId
                        )
                ) {

                    currentCase.put(
                            "expert_disease",
                            expertDisease
                    );

                    currentCase.put(
                            "expert_remarks",
                            expertRemarks
                    );

                    currentCase.put(
                            "status",
                            "REVIEWED"
                    );

                    currentCase.put(
                            "reviewed_at",
                            System.currentTimeMillis()
                    );

                    // Important:
                    // The farmer has not seen this
                    // newly reviewed result yet.
                    currentCase.put(
                            "viewed",
                            false
                    );

                    preferences.edit()
                            .putString(
                                    CASES_KEY,
                                    cases.toString()
                            )
                            .apply();

                    return true;
                }

            } catch (Exception e) {

                e.printStackTrace();

                return false;
            }
        }

        return false;
    }

    // ============================================================
    // MARK CASE AS VIEWED
    // ============================================================

    public static boolean markCaseAsViewed(
            Context context,
            String caseId
    ) {

        if (
                caseId == null
                        || caseId.trim().isEmpty()
        ) {

            return false;
        }

        SharedPreferences preferences =
                context.getSharedPreferences(
                        PREFS_NAME,
                        Context.MODE_PRIVATE
                );

        JSONArray cases =
                getAllCases(context);

        String currentFarmerMobile =
                getCurrentFarmerMobile(context);

        if (currentFarmerMobile.isEmpty()) {

            return false;
        }

        for (
                int i = 0;
                i < cases.length();
                i++
        ) {

            try {

                JSONObject currentCase =
                        cases.getJSONObject(i);

                String storedCaseId =
                        currentCase.optString(
                                "case_id",
                                ""
                        );

                String caseMobile =
                        currentCase.optString(
                                MOBILE_KEY,
                                ""
                        ).trim();

                if (
                        caseId.equals(
                                storedCaseId
                        )
                                && currentFarmerMobile.equals(
                                caseMobile
                        )
                ) {

                    currentCase.put(
                            "viewed",
                            true
                    );

                    preferences.edit()
                            .putString(
                                    CASES_KEY,
                                    cases.toString()
                            )
                            .apply();

                    return true;
                }

            } catch (Exception e) {

                e.printStackTrace();

                return false;
            }
        }

        return false;
    }

    // ============================================================
    // TOTAL CASES FOR CURRENT FARMER
    // ============================================================

    public static int getCurrentFarmerCaseCount(
            Context context
    ) {

        return getCasesForCurrentFarmer(
                context
        ).length();
    }

    // ============================================================
    // UNREAD EXPERT REVIEW ALERTS
    // ============================================================

    public static int getCurrentFarmerAlertCount(
            Context context
    ) {

        JSONArray farmerCases =
                getCasesForCurrentFarmer(
                        context
                );

        int count = 0;

        for (
                int i = 0;
                i < farmerCases.length();
                i++
        ) {

            try {

                JSONObject currentCase =
                        farmerCases.getJSONObject(i);

                String status =
                        currentCase.optString(
                                "status",
                                ""
                        );

                /*
                 * Older cases created before the
                 * "viewed" field was added will
                 * automatically be treated as unread.
                 */
                boolean viewed =
                        currentCase.optBoolean(
                                "viewed",
                                false
                        );

                if (
                        "REVIEWED".equals(
                                status
                        )
                                && !viewed
                ) {

                    count++;
                }

            } catch (Exception e) {

                e.printStackTrace();
            }
        }

        return count;
    }

    // ============================================================
    // TOTAL PENDING CASES
    // ============================================================

    public static int getPendingCount(
            Context context
    ) {

        JSONArray cases =
                getAllCases(context);

        int count = 0;

        for (
                int i = 0;
                i < cases.length();
                i++
        ) {

            try {

                JSONObject currentCase =
                        cases.getJSONObject(i);

                String status =
                        currentCase.optString(
                                "status",
                                ""
                        );

                if (
                        "PENDING".equals(
                                status
                        )
                ) {

                    count++;
                }

            } catch (Exception e) {

                e.printStackTrace();
            }
        }

        return count;
    }

    // ============================================================
    // HIGH PRIORITY PENDING CASES
    // ============================================================

    public static int getHighPriorityCount(
            Context context
    ) {

        JSONArray cases =
                getAllCases(context);

        int count = 0;

        for (
                int i = 0;
                i < cases.length();
                i++
        ) {

            try {

                JSONObject currentCase =
                        cases.getJSONObject(i);

                String status =
                        currentCase.optString(
                                "status",
                                ""
                        );

                String priority =
                        currentCase.optString(
                                "priority",
                                ""
                        );

                if (
                        "PENDING".equals(
                                status
                        )
                                && "HIGH".equals(
                                priority
                        )
                ) {

                    count++;
                }

            } catch (Exception e) {

                e.printStackTrace();
            }
        }

        return count;
    }
}