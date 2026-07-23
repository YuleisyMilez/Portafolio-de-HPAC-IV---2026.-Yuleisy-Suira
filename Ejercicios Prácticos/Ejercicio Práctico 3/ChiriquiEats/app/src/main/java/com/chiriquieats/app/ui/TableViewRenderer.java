package com.chiriquieats.app.ui;

import android.content.Context;
import android.graphics.Color;
import android.graphics.Typeface;
import android.view.Gravity;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.HorizontalScrollView;
import android.widget.ScrollView;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;

import java.util.List;

public final class TableViewRenderer {

    public interface OnRowClickListener {
        void onRowClick(String[] row);
    }

    private TableViewRenderer() {
    }

    public static void render(Context context, FrameLayout container, String[] headers,
                              List<String[]> rows, String emptyMessage) {
        render(context, container, headers, rows, emptyMessage, 0, null);
    }

    public static void render(Context context, FrameLayout container, String[] headers,
                              List<String[]> rows, String emptyMessage, int hiddenColumns,
                              OnRowClickListener listener) {
        container.removeAllViews();

        ScrollView verticalScroll = new ScrollView(context);
        HorizontalScrollView horizontalScroll = new HorizontalScrollView(context);
        TableLayout tableLayout = new TableLayout(context);
        tableLayout.setStretchAllColumns(false);
        tableLayout.setShrinkAllColumns(false);

        tableLayout.addView(createRow(context, headers, true));

        if (rows.isEmpty()) {
            TextView emptyView = createCell(context, emptyMessage, false);
            emptyView.setGravity(Gravity.CENTER);
            TableRow emptyRow = new TableRow(context);
            emptyRow.addView(emptyView);
            tableLayout.addView(emptyRow);
        } else {
            for (String[] row : rows) {
                TableRow tableRow = createRow(context, row, false, hiddenColumns);
                if (listener != null) {
                    tableRow.setClickable(true);
                    tableRow.setOnClickListener(view -> listener.onRowClick(row));
                }
                tableLayout.addView(tableRow);
            }
        }

        horizontalScroll.addView(tableLayout);
        verticalScroll.addView(horizontalScroll);
        container.addView(verticalScroll, new FrameLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT
        ));
    }

    private static TableRow createRow(Context context, String[] values, boolean isHeader) {
        return createRow(context, values, isHeader, 0);
    }

    private static TableRow createRow(Context context, String[] values, boolean isHeader,
                                      int hiddenColumns) {
        TableRow row = new TableRow(context);
        for (int index = hiddenColumns; index < values.length; index++) {
            String value = values[index];
            row.addView(createCell(context, value, isHeader));
        }
        return row;
    }

    private static TextView createCell(Context context, String value, boolean isHeader) {
        TextView cell = new TextView(context);
        cell.setText(value);
        cell.setTextSize(isHeader ? 14 : 13);
        cell.setTextColor(isHeader ? Color.WHITE : Color.rgb(30, 42, 36));
        cell.setTypeface(Typeface.DEFAULT, isHeader ? Typeface.BOLD : Typeface.NORMAL);
        cell.setGravity(Gravity.CENTER_VERTICAL);
        cell.setMinWidth(dp(context, 120));
        cell.setPadding(dp(context, 12), dp(context, 10), dp(context, 12), dp(context, 10));
        cell.setBackgroundColor(isHeader ? Color.rgb(15, 139, 87) : Color.WHITE);
        return cell;
    }

    private static int dp(Context context, int value) {
        return Math.round(value * context.getResources().getDisplayMetrics().density);
    }
}
