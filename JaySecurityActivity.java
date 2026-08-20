package com.jay.ai;

import android.app.Activity;
import android.os.Bundle;
import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.view.Gravity;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

public class JaySecurityActivity extends Activity {

    private JaySecurityManager securityManager;
    private TextView statusText;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        securityManager = new JaySecurityManager(this);

        buildScreen();
    }

    private void buildScreen() {

        LinearLayout root = new LinearLayout(this);

        root.setOrientation(LinearLayout.VERTICAL);
        root.setGravity(Gravity.CENTER);
        root.setPadding(30, 30, 30, 30);

        GradientDrawable background =
                new GradientDrawable(
                        GradientDrawable.Orientation.TL_BR,
                        new int[]{
                                Color.rgb(5, 5, 25),
                                Color.rgb(20, 10, 50),
                                Color.rgb(5, 25, 40)
                        }
                );

        root.setBackground(background);

        // -------------------------------------------------
        // TITLE
        // -------------------------------------------------

        TextView title = new TextView(this);

        title.setText("JAY SECURITY");
        title.setTextColor(Color.WHITE);
        title.setTextSize(30);
        title.setGravity(Gravity.CENTER);
        title.setTypeface(null, 1);

        root.addView(title);

        // -------------------------------------------------
        // JAY ICON
        // -------------------------------------------------

        TextView icon = new TextView(this);

        icon.setText("J");
        icon.setTextColor(Color.WHITE);
        icon.setTextSize(80);
        icon.setGravity(Gravity.CENTER);

        GradientDrawable circle =
                new GradientDrawable();

        circle.setShape(
                GradientDrawable.OVAL
        );

        circle.setColor(
                Color.rgb(15, 15, 45)
        );

        circle.setStroke(
                4,
                Color.rgb(100, 180, 255)
        );

        icon.setBackground(circle);

        LinearLayout.LayoutParams iconParams =
                new LinearLayout.LayoutParams(
                        200,
                        200
                );

        iconParams.gravity = Gravity.CENTER;

        root.addView(icon, iconParams);

        // -------------------------------------------------
        // STATUS
        // -------------------------------------------------

        statusText = new TextView(this);

        statusText.setTextColor(Color.WHITE);
        statusText.setTextSize(18);
        statusText.setGravity(Gravity.CENTER);
        statusText.setPadding(20, 30, 20, 30);

        root.addView(statusText);

        updateStatus();

        // -------------------------------------------------
        // ADMIN AUTHENTICATION
        // -------------------------------------------------

        Button adminButton = new Button(this);

        adminButton.setText(
                "🔐 VERIFY ADMIN"
        );

        adminButton.setOnClickListener(v ->
                authenticateAdmin()
        );

        root.addView(adminButton);

        // -------------------------------------------------
        // GUEST MODE
        // -------------------------------------------------

        Button guestButton = new Button(this);

        guestButton.setText(
                "👤 ENTER GUEST MODE"
        );

        guestButton.setOnClickListener(v -> {

            securityManager.enterGuestMode();

            updateStatus();

            Toast.makeText(
                    this,
                    "Guest Mode enabled",
                    Toast.LENGTH_SHORT
            ).show();
        });

        root.addView(guestButton);

        // -------------------------------------------------
        // RESTRICTED MODE
        // -------------------------------------------------

        Button restrictedButton =
                new Button(this);

        restrictedButton.setText(
                "🔒 RESTRICT PHONE ACCESS"
        );

        restrictedButton.setOnClickListener(v -> {

            securityManager
                    .enterRestrictedMode();

            updateStatus();

            Toast.makeText(
                    this,
                    "Jay Restricted Mode enabled",
                    Toast.LENGTH_SHORT
            ).show();
        });

        root.addView(restrictedButton);

        // -------------------------------------------------
        // BACK
        // -------------------------------------------------

        Button backButton = new Button(this);

        backButton.setText("BACK");

        backButton.setOnClickListener(v ->
                finish()
        );

        root.addView(backButton);

        setContentView(root);
    }

    // -----------------------------------------------------
    // ADMIN AUTHENTICATION
    // -----------------------------------------------------

    private void authenticateAdmin() {

        securityManager.authenticateAdmin(
                new JaySecurityManager.AuthorizationCallback() {

                    @Override
                    public void onAuthorized() {

                        updateStatus();

                        Toast.makeText(
                                JaySecurityActivity.this,
                                "Welcome back, Admin 👑",
                                Toast.LENGTH_SHORT
                        ).show();
                    }

                    @Override
                    public void onDenied() {

                        updateStatus();

                        Toast.makeText(
                                JaySecurityActivity.this,
                                "Admin verification failed",
                                Toast.LENGTH_SHORT
                        ).show();
                    }
                }
        );
    }

    // -----------------------------------------------------
    // STATUS
    // -----------------------------------------------------

    private void updateStatus() {

        String status =
                securityManager.getSecurityStatus();

        if ("ADMIN".equals(status)) {

            statusText.setText(
                    "👑 ADMIN MODE\n\n" +
                    "Jay recognizes you as Admin."
            );

        } else if ("GUEST".equals(status)) {

            statusText.setText(
                    "👤 GUEST MODE\n\n" +
                    "Restricted access is active."
            );

        } else {

            statusText.setText(
                    "🔒 LOCKED\n\n" +
                    "Admin authentication required."
            );
        }
    }
                        }
