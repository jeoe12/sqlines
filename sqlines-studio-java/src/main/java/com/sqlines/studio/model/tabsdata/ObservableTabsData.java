/*
 * Copyright (c) 2021 SQLines
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.sqlines.studio.model.tabsdata;

import com.sqlines.studio.model.tabsdata.listener.TabIndexChangeListener;
import com.sqlines.studio.model.tabsdata.listener.TabTitleChangeListener;
import com.sqlines.studio.model.tabsdata.listener.TabsChangeListener;
import com.sqlines.studio.model.tabsdata.listener.TextChangeListener;
import com.sqlines.studio.model.tabsdata.listener.ModeChangeListener;
import com.sqlines.studio.model.tabsdata.listener.FilePathChangeListener;

import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.io.Serializable;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Objects;

/**
 * Contains a synchronized observable list with the data of opened tabs.
 * <p>
 * Allows listeners to track changes when they occur.
 *
 * @see TabsChangeListener
 * @see TabIndexChangeListener
 * @see TabTitleChangeListener
 * @see ModeChangeListener
 * @see TextChangeListener
 * @see FilePathChangeListener
 */
public class ObservableTabsData implements Serializable {

    private static class TabData implements Serializable {
        String tabTitle = "";
        String sourceText = "";
        String targetText = "";
        String sourceMode = "";
        String targetMode = "";
        String sourceFilePath = "";
        String targetFilePath = "";

        @Override
        public boolean equals(Object other) {
            if (this == other) {
                return true;
            }

            if (other == null || getClass() != other.getClass()) {
                return false;
            }

            TabData data = (TabData) other;
            return Objects.equals(tabTitle, data.tabTitle)
                    && Objects.equals(sourceText, data.sourceText)
                    && Objects.equals(targetText, data.targetText)
                    && Objects.equals(sourceMode, data.sourceMode)
                    && Objects.equals(targetMode, data.targetMode)
                    && Objects.equals(sourceFilePath, data.sourceFilePath)
                    && Objects.equals(targetFilePath, data.targetFilePath);
        }

        @Override
        public int hashCode() {
            return Objects.hash(tabTitle, sourceText, targetText,
                    sourceMode, targetMode, sourceFilePath, targetFilePath);
        }
    }

    private static final long serialVersionUID = 498374478;

    private List<TabData> tabsData = new LinkedList<>();
    private int currTabIndex = -1;

    private List<TabsChangeListener> tabsListeners = new ArrayList<>(5);
    private List<TabIndexChangeListener> tabIndexListeners = new ArrayList<>(5);
    private List<TabTitleChangeListener> titleListeners = new ArrayList<>(5);
    private List<TextChangeListener> sourceTextListeners = new ArrayList<>(5);
    private List<TextChangeListener> targetTextListeners = new ArrayList<>(5);
    private List<ModeChangeListener> sourceModeListeners = new ArrayList<>(5);
    private List<ModeChangeListener> targetModeListeners = new ArrayList<>(5);
    private List<FilePathChangeListener> sourcePathListeners = new ArrayList<>(5);
    private List<FilePathChangeListener> targetPathListeners = new ArrayList<>(5);

    /**
     * Adds new tab at the specified position in the list of tabs.
     * <p>
     * Notifies all {@link TabsChangeListener} listeners of the change.
     *
     * @param tabIndex the index at which a tab is to be inserted
     *
     * @throws IndexOutOfBoundsException if the index is out of range
     * (index < 0 || index >= countTabs())
     */
    public synchronized void openTab(int tabIndex) {
        // Throws exception if tabIndex is out of valid range
        checkRange(tabIndex, 0, tabsData.size() + 1);

        tabsData.add(tabIndex, new TabData());
        TabsChangeListener.Change added = new TabsChangeListener.Change(
                TabsChangeListener.Change.ChangeType.TAB_ADDED, tabIndex
        );
        tabsListeners.forEach(listener -> listener.onChange(added));
    }

    /**
     * Removes the tab at the specified position.
     * <p>
     * Notifies all {@link TabsChangeListener} listeners of the change.
     *
     * @param tabIndex the index of the tab to be removed
     *
     * @throws IndexOutOfBoundsException if the index is out of range
     * (index < 0 || index >= countTabs())
     */
    public synchronized void removeTab(int tabIndex) {
        // Throws exception if tabIndex is out of valid range
        checkRange(tabIndex, 0, tabsData.size());

        tabsData.remove(tabIndex);
        TabsChangeListener.Change removed = new TabsChangeListener.Change(
                TabsChangeListener.Change.ChangeType.TAB_REMOVED, tabIndex
        );
        tabsListeners.forEach(listener -> listener.onChange(removed));
    }

    /**
     * Removes all tabs.
     * <p>
     * Notifies all {@link TabsChangeListener} listeners of the change.
     */
    public synchronized void removeAllTabs() {
        int tabsNumber = tabsData.size();
        tabsData.clear();
        currTabIndex = -1;

        for (int i = 0; i < tabsNumber; i++) {
            TabsChangeListener.Change removed = new TabsChangeListener.Change(
                    TabsChangeListener.Change.ChangeType.TAB_REMOVED, 0
            );
            tabsListeners.forEach(listener -> listener.onChange(removed));
        }
    }

    /**
     * @return the number of tabs
     */
    public synchronized int countTabs() {
        return tabsData.size();
    }

    /**
     * @return the index of the current working tab
     */
    public synchronized int getCurrTabIndex() {
        return currTabIndex;
    }

    /**
     * Returns the title of the specified tab.
     *
     * @param tabIndex the index of the tab with title to return
     *
     * @return the title of the specified tab
     *
     * @throws IndexOutOfBoundsException if the index is out of range
     * (index < 0 || index >= countTabs())
     */
    public synchronized @NotNull String getTabTitle(int tabIndex) {
        // Throws exception if tabIndex is out of valid range
        checkRange(tabIndex, 0, tabsData.size());
        return tabsData.get(tabIndex).tabTitle;
    }

    /**
     * Returns the source text in the specified tab.
     *
     * @param tabIndex the index of the tab with source text to return
     *
     * @return the source text in the specified tab
     *
     * @throws IndexOutOfBoundsException if the index is out of range
     * (index < 0 || index >= countTabs())
     */
    public synchronized @NotNull String getSourceText(int tabIndex) {
        // Throws exception if tabIndex is out of valid range
        checkRange(tabIndex, 0, tabsData.size());
        return tabsData.get(tabIndex).sourceText;
    }

    /**
     * Returns the target text in the specified tab.
     *
     * @param tabIndex the index of the tab with target text to return
     *
     * @return the target text in the specified tab
     *
     * @throws IndexOutOfBoundsException if the index is out of range
     * (index < 0 || index >= countTabs())
     */
    public synchronized  @NotNull String getTargetText(int tabIndex) {
        // Throws exception if tabIndex is out of valid range
        checkRange(tabIndex, 0, tabsData.size());
        return tabsData.get(tabIndex).targetText;
    }

    /**
     * Returns the source mode in the specified tab.
     *
     * @param tabIndex the index of the tab with source mode to return
     *
     * @return the source mode in the specified tab
     *
     * @throws IndexOutOfBoundsException if the index is out of range
     * (index < 0 || index >= countTabs())
     */
    public synchronized @NotNull String getSourceMode(int tabIndex) {
        // Throws exception if tabIndex is out of valid range
        checkRange(tabIndex, 0, tabsData.size());
        return tabsData.get(tabIndex).sourceMode;
    }

    /**
     * Returns the target mode in the specified tab.
     *
     * @param tabIndex the index of the tab with target mode to return
     *
     * @return the target mode in the specified tab
     *
     * @throws IndexOutOfBoundsException if the index is out of range
     * (index < 0 || index >= countTabs())
     */
    public synchronized @NotNull String getTargetMode(int tabIndex) {
        // Throws exception if tabIndex is out of valid range
        checkRange(tabIndex, 0, tabsData.size());
        return tabsData.get(tabIndex).targetMode;
    }

    /**
     * Returns the source file path in the specified tab.
     *
     * @param tabIndex the index of the tab with source file path to return
     *
     * @return the source file path in the specified tab
     *
     * @throws IndexOutOfBoundsException if the index is out of range
     * (index < 0 || index >= countTabs())
     */
    public synchronized @NotNull String getSourceFilePath(int tabIndex) {
        // Throws exception if tabIndex is out of valid range
        checkRange(tabIndex, 0, tabsData.size());
        return tabsData.get(tabIndex).sourceFilePath;
    }

    /**
     * Returns the target file path in the specified tab.
     *
     * @param tabIndex the index of the tab with target file path to return
     *
     * @return the target file path in the specified tab
     *
     * @throws IndexOutOfBoundsException if the index is out of range
     * (index < 0 || index >= countTabs())
     */
    public synchronized @NotNull String getTargetFilePath(int tabIndex) {
        // Throws exception if tabIndex is out of valid range
        checkRange(tabIndex, 0, tabsData.size());
        return tabsData.get(tabIndex).targetFilePath;
    }

    /**
     * Sets the specified tab index as current.
     * <p>
     * Notifies all {@link TabIndexChangeListener} listeners of the change.
     *
     * @param tabIndex tab index to set
     *
     * @throws IndexOutOfBoundsException if the index is out of range
     * (index < 0 || index >= countTabs())
     */
    public synchronized void setCurrTabIndex(int tabIndex) {
        // Throws exception if tabIndex is out of valid range
        checkRange(tabIndex, 0, tabsData.size());

        currTabIndex = tabIndex;
        tabIndexListeners.forEach(listener -> listener.changed(currTabIndex));
    }

    /**
     * Sets the tab title of the specified tab.
     * <p>
     * Notifies all {@link TabTitleChangeListener} listeners of the change.
     *
     * @param title title to set
     * @param tabIndex the index of the tab with title to set
     *
     * @throws IndexOutOfBoundsException if the index is out of range
     * (index < 0 || index >= countTabs())
     */
    public synchronized void setTabTitle(@NotNull String title, int tabIndex) {
        // Throws exception if tabIndex is out of valid range
        checkRange(tabIndex, 0, tabsData.size());

        tabsData.get(tabIndex).tabTitle = title;
        titleListeners.forEach(listener -> listener.changed(title, tabIndex));
    }

    /**
     * Sets the source text in the specified tab.
     * <p>
     * Notifies all source {@link TextChangeListener} listeners of the change.
     *
     * @param text text to set
     * @param tabIndex the index of the tab with source text to set
     *
     * @throws IndexOutOfBoundsException if the index is out of range
     * (index < 0 || index >= countTabs())
     */
    public synchronized void setSourceText(@NotNull String text, int tabIndex) {
        // Throws exception if tabIndex is out of valid range
        checkRange(tabIndex, 0, tabsData.size());

        tabsData.get(tabIndex).sourceText = text;
        sourceTextListeners.forEach(listener -> listener.changed(text, tabIndex));
    }

    /**
     * Sets the target text in the specified tab.
     * <p>
     * Notifies all target {@link TextChangeListener} listeners of the change.
     *
     * @param text text to set
     * @param tabIndex the index of the tab with target text to set
     *
     * @throws IndexOutOfBoundsException if the index is out of range
     * (index < 0 || index >= countTabs())
     */
    public synchronized void setTargetText(@NotNull String text, int tabIndex) {
        // Throws exception if tabIndex is out of valid range
        checkRange(tabIndex, 0, tabsData.size());

        tabsData.get(tabIndex).targetText = text;
        targetTextListeners.forEach(listener -> listener.changed(text, tabIndex));
    }

    /**
     * Sets the source conversion mode in the specified tab.
     * <p>
     * Notifies all source {@link ModeChangeListener} listeners of the change.
     *
     * @param mode source mode to set
     * @param tabIndex the index of the tab with source mode to set
     *
     * @throws IndexOutOfBoundsException if the index is out of range
     * (index < 0 || index >= countTabs())
     */
    public synchronized void setSourceMode(@NotNull String mode, int tabIndex) {
        // Throws exception if tabIndex is out of valid range
        checkRange(tabIndex, 0, tabsData.size());

        tabsData.get(tabIndex).sourceMode = mode;
        sourceModeListeners.forEach(listener -> listener.changed(mode, tabIndex));
    }

    /**
     * Sets the target conversion mode in the specified tab.
     * <p>
     * Notifies all target {@link ModeChangeListener} listeners of the change.
     *
     * @param mode target mode to set
     * @param tabIndex the index of the tab with target mode to set
     *
     * @throws IndexOutOfBoundsException if the index is out of range
     * (index < 0 || index >= countTabs())
     */
    public synchronized void setTargetMode(@NotNull String mode, int tabIndex) {
        // Throws exception if tabIndex is out of valid range
        checkRange(tabIndex, 0, tabsData.size());

        tabsData.get(tabIndex).targetMode = mode;
        targetModeListeners.forEach(listener -> listener.changed(mode, tabIndex));
    }

    /**
     * Sets the source file path in the specified tab.
     * <p>
     * Notifies all source {@link FilePathChangeListener} listeners of the change.
     *
     * @param filePath file path to set
     * @param tabIndex the index of the tab with source file path to set
     *
     * @throws IndexOutOfBoundsException if the index is out of range
     * (index < 0 || index >= countTabs())
     */
    public synchronized void setSourceFilePath(@NotNull String filePath, int tabIndex) {
        // Throws exception if tabIndex is out of valid range
        checkRange(tabIndex, 0, tabsData.size());

        tabsData.get(tabIndex).sourceFilePath = filePath;
        sourcePathListeners.forEach(listener -> listener.changed(filePath, tabIndex));
    }

    /**
     * Sets the target file path in the specified tab.
     * <p>
     * Notifies all target {@link FilePathChangeListener} listeners of the change.
     *
     * @param filePath file path to set
     * @param tabIndex the index of the tab with target file path to set
     *
     * @throws IndexOutOfBoundsException if the index is out of range
     * (index < 0 || index >= countTabs())
     */
    public synchronized void setTargetFilePath(@NotNull String filePath, int tabIndex) {
        // Throws exception if tabIndex is out of valid range
        checkRange(tabIndex, 0, tabsData.size());

        tabsData.get(tabIndex).targetFilePath = filePath;
        targetPathListeners.forEach(listener -> listener.changed(filePath, tabIndex));
    }

    /**
     * Adds a listener which will be notified when the list of the tabs changes.
     * If the same listener is added more than once, then it will be notified more than once.
     *
     * @param listener the listener to register
     */
    public synchronized void addTabsListener(@NotNull TabsChangeListener listener) {
        tabsListeners.add(listener);
    }

    /**
     * Removes the specified listener
     *
     * @param listener the listener to remove
     */
    public synchronized void removeTabsListener(@NotNull TabsChangeListener listener) {
        tabsListeners.remove(listener);
    }

    /**
     * Adds a listener which will be notified when the index of the current tab changes.
     * If the same listener is added more than once, then it will be notified more than once.
     *
     * @param listener the listener to register
     */
    public synchronized void addTabIndexListener(@NotNull TabIndexChangeListener listener) {
        tabIndexListeners.add(listener);
    }

    /**
     * Removes the specified listener
     *
     * @param listener the listener to remove
     */
    public synchronized void removeTabIndexListener(@NotNull TabIndexChangeListener listener) {
        tabIndexListeners.remove(listener);
    }

    /**
     * Adds a listener which will be notified when the tab title changes.
     * If the same listener is added more than once, then it will be notified more than once.
     *
     * @param listener the listener to register
     */
    public synchronized void addTabTitleListener(@NotNull TabTitleChangeListener listener) {
        titleListeners.add(listener);
    }

    /**
     * Removes the specified listener
     *
     * @param listener the listener to remove
     */
    public synchronized void removeTabTitleListener(@NotNull TabTitleChangeListener listener) {
        titleListeners.remove(listener);
    }

    /**
     * Adds a listener which will be notified when the source mode changes.
     * If the same listener is added more than once, then it will be notified more than once.
     *
     * @param listener the listener to register
     */
    public synchronized void addSourceModeListener(@NotNull ModeChangeListener listener) {
        sourceModeListeners.add(listener);
    }

    /**
     * Removes the specified listener
     *
     * @param listener the listener to remove
     */
    public synchronized void removeSourceModeListener(@NotNull ModeChangeListener listener) {
        sourceModeListeners.remove(listener);
    }

    /**
     * Adds a listener which will be notified when the target mode changes.
     * If the same listener is added more than once, then it will be notified more than once.
     *
     * @param listener the listener to register
     */
    public synchronized void addTargetModeListener(@NotNull ModeChangeListener listener) {
        targetModeListeners.add(listener);
    }

    /**
     * Removes the specified listener
     *
     * @param listener the listener to remove
     */
    public synchronized void removeTargetModeListener(@NotNull ModeChangeListener listener) {
        targetModeListeners.remove(listener);
    }

    /**
     * Adds a listener which will be notified when the source text changes.
     * If the same listener is added more than once, then it will be notified more than once.
     *
     * @param listener the listener to register
     */
    public synchronized void addSourceTextListener(@NotNull TextChangeListener listener) {
        sourceTextListeners.add(listener);
    }

    /**
     * Removes the specified listener
     *
     * @param listener the listener to remove
     */
    public synchronized void removeSourceTextListener(@NotNull TextChangeListener listener) {
        sourceTextListeners.remove(listener);
    }

    /**
     * Adds a listener which will be notified when the target text changes.
     * If the same listener is added more than once, then it will be notified more than once.
     *
     * @param listener the listener to register
     */
    public synchronized void addTargetTextListener(@NotNull TextChangeListener listener) {
        targetTextListeners.add(listener);
    }

    /**
     * Removes the specified listener
     *
     * @param listener the listener to remove
     */
    public synchronized void removeTargetTextListener(@NotNull TextChangeListener listener) {
        targetTextListeners.remove(listener);
    }

    /**
     * Adds a listener which will be notified when the source file path changes.
     * If the same listener is added more than once, then it will be notified more than once.
     *
     * @param listener the listener to register
     */
    public synchronized void addSourceFilePathListener(@NotNull FilePathChangeListener listener) {
        sourcePathListeners.add(listener);
    }

    /**
     * Adds a listener which will be notified when the target file path changes.
     * If the same listener is added more than once, then it will be notified more than once.
     *
     * @param listener the listener to register
     */
    public synchronized void addTargetFilePathListener(@NotNull FilePathChangeListener listener) {
        targetPathListeners.add(listener);
    }

    @Override
    public synchronized boolean equals(Object other) {
        if (this == other) {
            return true;
        }

        if (other == null || getClass() != other.getClass()) {
            return false;
        }

        ObservableTabsData data = (ObservableTabsData) other;
        return currTabIndex == data.currTabIndex && Objects.equals(tabsData, data.tabsData);
    }

    @Override
    public synchronized int hashCode() {
        return Objects.hash(tabsData, currTabIndex);
    }

    private void checkRange(int tabIndex, int from, int to) {
        if (tabIndex < from || tabIndex >= to) {
            int endInd = (tabsData.size() == 0) ? 0 : tabsData.size() - 1;
            String errorMsg = "Invalid index: " + "(0:" + endInd + ") expected, "
                    + tabIndex + " provided";
            throw new IndexOutOfBoundsException(errorMsg);
        }
    }
    
    private synchronized void readObject(@NotNull ObjectInputStream stream)
            throws ClassNotFoundException, IOException {
        tabsListeners = new ArrayList<>(5);
        tabIndexListeners = new ArrayList<>(5);
        titleListeners = new ArrayList<>(5);
        sourceTextListeners = new ArrayList<>(5);
        targetTextListeners = new ArrayList<>(5);
        sourceModeListeners = new ArrayList<>(5);
        targetModeListeners = new ArrayList<>(5);
        sourcePathListeners = new ArrayList<>(5);
        targetPathListeners = new ArrayList<>(5);
        tabsData = new LinkedList<>();
        currTabIndex = -1;

        int tabsNumber = stream.readInt();
        for (int i = 0; i < tabsNumber; i++) {
            TabData data = (TabData) stream.readObject();
            tabsData.add(data);
        }

        currTabIndex = stream.readInt();
    }

    private synchronized void writeObject(@NotNull ObjectOutputStream stream) throws IOException {
        stream.writeInt(tabsData.size());
        for (TabData data : tabsData) {
            stream.writeObject(data);
        }

        stream.writeInt(currTabIndex);
    }
}
