package com.example.demo11;

import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.IconLoader;
import com.intellij.openapi.wm.ToolWindow;
import com.intellij.openapi.wm.ToolWindowFactory;
import org.jetbrains.annotations.NotNull;
import com.intellij.ui.content.ContentFactory;
import com.intellij.ui.content.Content;

import javax.swing.*;
import javax.swing.border.Border;
import java.awt.*;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.StringSelection;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.List;

public class SidepanelFactory implements ToolWindowFactory {

    private static final String OPENAI_API_KEY = "sk-XXXXXXXXXXXXXXXX";  // Replace with your OpenAI API key

    @Override
    public void createToolWindowContent(@NotNull Project project, @NotNull ToolWindow toolWindow) {
        JPanel mainPanel = new JPanel();
        mainPanel.setLayout(new BorderLayout());

        // Create header panel with dropdown and buttons
        JPanel headerPanel = new JPanel();
        headerPanel.setLayout(new BorderLayout()); // Use BorderLayout for better control over positioning

        // Left panel for the label and combo box
        JPanel leftPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        JLabel modelLabel = new JLabel("Model: ");
        String[] modelOptions = {"Professional", "Simple"};
        JComboBox<String> modelComboBox = new JComboBox<>(modelOptions);

        leftPanel.add(modelLabel);
        leftPanel.add(modelComboBox);

        // Add the left panel to the left side of the header
        headerPanel.add(leftPanel, BorderLayout.WEST);

        // Right panel for the buttons
        JPanel rightPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));

        // Refresh button
        JButton refreshButton = new JButton(IconLoader.getIcon("/icons/refresh.png"));
        refreshButton.setBorderPainted(false);
        refreshButton.setContentAreaFilled(false);

        // Back button
        JButton backButton = new JButton(IconLoader.getIcon("/icons/refresh.png"));
        backButton.setBorderPainted(false);
        backButton.setContentAreaFilled(false);
        backButton.setVisible(false); // Hide back button initially

        rightPanel.add(refreshButton);
        rightPanel.add(backButton); // Add both buttons to the right panel

        // Add the right panel to the east side of the header
        headerPanel.add(rightPanel, BorderLayout.EAST);

        // Chat panel for displaying messages
        JPanel chatPanel = new JPanel();
        chatPanel.setLayout(new BoxLayout(chatPanel, BoxLayout.Y_AXIS));
        JScrollPane scrollPane = new JScrollPane(chatPanel);
        scrollPane.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS);
        mainPanel.add(scrollPane, BorderLayout.CENTER);

        // Input panel for user input
        JPanel inputPanel = new JPanel();
        inputPanel.setLayout(new BoxLayout(inputPanel, BoxLayout.X_AXIS));

        JTextArea inputTextArea = new JTextArea(2, 30);
        inputTextArea.setWrapStyleWord(true);
        inputTextArea.setLineWrap(true);
        inputTextArea.setBorder(BorderFactory.createLineBorder(Color.GRAY));
        inputTextArea.setPreferredSize(new Dimension(250, 40));

        JButton submitButton = new JButton("Ask AI");

        // Add a loading icon (JLabel) next to the submit button
        JLabel loadingIconLabel = new JLabel();
        loadingIconLabel.setIcon(IconLoader.getIcon("/icons/loading.gif"));
        loadingIconLabel.setVisible(false);

        // Add components to the input panel (text area, button, and loading icon)
        inputPanel.add(inputTextArea);
        inputPanel.add(submitButton);
        inputPanel.add(loadingIconLabel);

        mainPanel.add(inputPanel, BorderLayout.SOUTH);

        // Add the header panel to the top of the main panel
        mainPanel.add(headerPanel, BorderLayout.NORTH);

        // Action for refresh button
        refreshButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                // Hide refresh button and show back button
                refreshButton.setVisible(false);
                backButton.setVisible(true);

                // Remove the old content (chat panel and input panel)
                mainPanel.remove(scrollPane);
                mainPanel.remove(inputPanel);

                // Create a new panel with some dummy text
                JPanel dummyPanel = new JPanel();
                JLabel dummyTextLabel = new JLabel("This is some dummy text after refresh.");
                dummyPanel.add(dummyTextLabel);

                // Add the new panel with dummy text to the mainPanel
                mainPanel.add(dummyPanel, BorderLayout.CENTER);

                // Revalidate and repaint the main panel to show the updated content
                mainPanel.revalidate();
                mainPanel.repaint();
            }
        });

        // Action for back button
        backButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                // Show the refresh button and hide the back button
                refreshButton.setVisible(true);
                backButton.setVisible(false);

                // Remove the dummy panel with the text
                mainPanel.removeAll();

                // Re-add the original content (chat panel and input panel)
                mainPanel.add(headerPanel, BorderLayout.NORTH);
                mainPanel.add(scrollPane, BorderLayout.CENTER);
                mainPanel.add(inputPanel, BorderLayout.SOUTH);

                // Revalidate and repaint to show the original panels
                mainPanel.revalidate();
                mainPanel.repaint();
            }
        });

        // Action for submitting the user input
        submitButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                showNewAIContainer = true;
                String userInput = inputTextArea.getText().trim();
                if (!userInput.isEmpty()) {
                    addUserMessage(chatPanel, userInput);
                    loadingIconLabel.setVisible(true);
                    new AIResponseWorker(chatPanel, userInput, project, inputTextArea, loadingIconLabel).execute();
                }
                inputTextArea.setText("");
            }
        });

        // Set empty title to remove "AI Assistant" from the top action bar
        Content content = ContentFactory.getInstance().createContent(mainPanel, "", false);
        toolWindow.getContentManager().addContent(content);
    }






    // Method to add the user message to the chat panel
    private void addUserMessage(JPanel chatPanel, String message) {
//        JPanel userMessagePanel = new JPanel();
//        userMessagePanel.setLayout(new BoxLayout(userMessagePanel, BoxLayout.X_AXIS));
//        JLabel userLabel = new JLabel("<html><b>You:</b> " + message + "</html>");
//        userLabel.setOpaque(true);
//        userLabel.setBorder(BorderFactory.createEmptyBorder(5, 10, 5, 10));
//        userMessagePanel.add(userLabel);
//        userMessagePanel.add(Box.createHorizontalGlue());
//        chatPanel.add(userMessagePanel);
//        chatPanel.revalidate();
//        chatPanel.repaint();

        JTextArea userMessagePanel = new JTextArea();
        userMessagePanel.setText("You: "+message);
        userMessagePanel.setEditable(false);
        userMessagePanel.setLineWrap(true);
        userMessagePanel.setWrapStyleWord(true);
        userMessagePanel.setBorder(BorderFactory.createEmptyBorder(5, 10, 5, 10)); // Inner padding for text

        JScrollPane scrollPane = new JScrollPane(userMessagePanel);
        scrollPane.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
        scrollPane.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS);

        // Set an initial preferred size for small content
        scrollPane.setPreferredSize(new Dimension(400, 100)); // Width 400px, Height 100px

        // Create a margin border for spacing
        Border marginBorder = BorderFactory.createEmptyBorder(10, 15, 10, 15); // Top, Left, Bottom, Right

        // Create a rounded border
        Border roundedBorder = new RoundedBorder(15); // 15 is the radius of the rounded corners

        // Combine the borders
        Border combinedBorder = BorderFactory.createCompoundBorder(marginBorder, roundedBorder);

        // Apply the combined border to the JScrollPane
        scrollPane.setBorder(combinedBorder);

        // Add the scroll pane to the chat panel
        chatPanel.add(scrollPane, BorderLayout.CENTER);
        chatPanel.revalidate();
        chatPanel.repaint();
    }

    // Declare JTextArea at the class level to persist across updates
    private JTextArea aiTextArea;
    private boolean showNewAIContainer = true;
    private void addAIResponse(JPanel chatPanel, String newMessage) {
        if (showNewAIContainer) {
            aiTextArea = new JTextArea();
            aiTextArea.setText("AI:");
            aiTextArea.setEditable(false);
            aiTextArea.setLineWrap(true);
            aiTextArea.setWrapStyleWord(true);
            aiTextArea.setBorder(BorderFactory.createEmptyBorder(5, 10, 5, 10)); // Inner padding for text

            JScrollPane scrollPane = new JScrollPane(aiTextArea);
            scrollPane.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
            scrollPane.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS);

            // Create a margin border for spacing
            Border marginBorder = BorderFactory.createEmptyBorder(10, 15, 10, 15); // Top, Left, Bottom, Right

            // Create a rounded border
            Border roundedBorder = new RoundedBorder(15); // 15 is the radius of the rounded corners

            // Combine the borders
            Border combinedBorder = BorderFactory.createCompoundBorder(marginBorder, roundedBorder);

            // Apply the combined border to the JScrollPane
            scrollPane.setBorder(combinedBorder);

            // Add the scroll pane to the chat panel
            chatPanel.add(scrollPane, BorderLayout.CENTER);
            showNewAIContainer = false;
        }

        // Concatenate the new response to the existing JTextArea content
        String currentText = aiTextArea.getText();
        String updatedText = currentText + newMessage + "\n";
        aiTextArea.setText(updatedText);

        // Revalidate and repaint the chat panel
        chatPanel.revalidate();
        chatPanel.repaint();

        // Scroll the chat panel to the bottom
        JScrollPane scrollPane = getScrollPane(chatPanel);
        if (scrollPane != null) {
            // After revalidating and adding components, explicitly scroll to the bottom
            SwingUtilities.invokeLater(() -> {
                JScrollBar verticalScrollBar = scrollPane.getVerticalScrollBar();
                verticalScrollBar.setValue(verticalScrollBar.getMaximum()); // Scroll to the bottom
            });
        }
    }

    // Utility method to find the JScrollPane in the parent hierarchy
    private JScrollPane getScrollPane(JPanel chatPanel) {
        // Traverse parent components to find JScrollPane
        Component parent = chatPanel.getParent();
        while (parent != null) {
            if (parent instanceof JScrollPane) {
                return (JScrollPane) parent;
            }
            parent = parent.getParent();
        }
        return null;
    }

    // SwingWorker to handle API request and streaming response
    private class AIResponseWorker extends SwingWorker<Void, String> {

        private final JPanel chatPanel;
        private final String userInput;
        private final Project project;
        private final JTextArea inputTextArea;
        private final JLabel loadingIconLabel;
        private final StringBuilder accumulatedResponse;

        public AIResponseWorker(JPanel chatPanel, String userInput, Project project, JTextArea inputTextArea, JLabel loadingIconLabel) {
            this.chatPanel = chatPanel;
            this.userInput = userInput;
            this.project = project;
            this.inputTextArea = inputTextArea;
            this.loadingIconLabel = loadingIconLabel;
            this.accumulatedResponse = new StringBuilder();
        }

        @Override
        protected Void doInBackground() throws Exception {
            // API URL setup (replace with actual URL and use POST method if required)
            String apiUrl = "http://localhost:3000/stream-chat";  // Replace with actual URL if necessary
            URL url = new URL(apiUrl);
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("POST");
            connection.setConnectTimeout(5000);
            connection.setReadTimeout(5000);
            connection.setRequestProperty("Content-Type", "application/json");
            connection.setDoOutput(true);  // Set to true to send a request body

            // Create JSON payload (example JSON object)
            String jsonPayload = "{ \"text\": \"write a java code\" }";  // Example payload, customize as needed

            // Write the JSON payload to the output stream
            try (OutputStream os = connection.getOutputStream()) {
                byte[] input = jsonPayload.getBytes("utf-8");
                os.write(input, 0, input.length);  // Send the request body
            }

            // Open connection and read the response
            BufferedReader in = new BufferedReader(new InputStreamReader(connection.getInputStream()));
            StringBuilder accumulatedResponse = new StringBuilder();
            String inputLine;
            while ((inputLine = in.readLine()) != null) {
                // Publish the accumulated response so that process() is called
                accumulatedResponse.append(inputLine+"\n");
//                publish(accumulatedResponse.toString());
                publish(inputLine.toString());
            }
//            in.close();
            onResponseComplete(String.valueOf(accumulatedResponse));
            return null;
        }

        private void onResponseComplete(String accumulatedResponse) {
            // Code to handle the completion of the response
            // Example: Hide loading indicator, enable buttons, etc.
            SwingUtilities.invokeLater(new Runnable() {
                @Override
                public void run() {
                    // Assuming you have a loading indicator like a JLabel
                    loadingIconLabel.setVisible(false);
//                    publish(accumulatedResponse.toString());
                    addAIResponseWithHighlighting(accumulatedResponse.toString());
                    // Perform any other UI updates that need to be done after response is complete
                }
            });
        }

        private void addAIResponseWithHighlighting(String message) {
            JPanel aiResponsePanel = new JPanel();
            aiResponsePanel.setLayout(new BoxLayout(aiResponsePanel, BoxLayout.Y_AXIS)); // Stack content vertically
            aiResponsePanel.setBorder(BorderFactory.createEmptyBorder(10, 15, 10, 15)); // Outer spacing

            String[] parts = message.split("```"); // Split message into code and text blocks

            for (int i = 0; i < parts.length; i++) {
                JPanel contentPanel = new JPanel();
                contentPanel.setLayout(new BorderLayout()); // Layout for content

                if (i % 2 == 0) {
                    // Regular text block (not a code block)
                    JTextArea textArea = new JTextArea(parts[i]);
                    textArea.setEditable(false);
                    textArea.setLineWrap(true);
                    textArea.setWrapStyleWord(true);
                    textArea.setBorder(BorderFactory.createEmptyBorder(5, 10, 5, 10)); // Inner padding
//                    textArea.setBackground(Color.WHITE); // Background for normal text
//                    textArea.setForeground(Color.BLACK); // Text color
                    contentPanel.add(textArea, BorderLayout.CENTER); // Add to content panel
                } else {
                    // Code block (inside backticks)
                    JPanel codePanel = new JPanel();
                    codePanel.setLayout(new BorderLayout()); // Layout for code block panel

                    codePanel.setBackground(new Color(43, 43, 43)); // Dark background for code
                    codePanel.setForeground(Color.WHITE); // Light text color

                    // Create the "Copy" button (inside code block background)
                    JButton copyButton = new JButton("Copy");
                    copyButton.setMargin(new Insets(0, 0, 0, 0)); // Remove margins
                    copyButton.setPreferredSize(new Dimension(60, 20)); // Set fixed size for the button
                    int finalI = i;
                    copyButton.addActionListener(e -> {
                        StringSelection selection = new StringSelection(parts[finalI]);
                        Clipboard clipboard = Toolkit.getDefaultToolkit().getSystemClipboard();
                        clipboard.setContents(selection, null); // Copy the code to clipboard
                    });

                    // Create the image label (optional image on the top right of the code block)
                    JLabel imageLabel = new JLabel(new ImageIcon("path_to_your_image.png"));
                    imageLabel.setHorizontalAlignment(SwingConstants.RIGHT); // Align image to the right

                    // Create a panel for the button and image (inside the code block)
                    JPanel topRightPanel = new JPanel();
                    topRightPanel.setLayout(new BoxLayout(topRightPanel, BoxLayout.X_AXIS));
                    topRightPanel.setOpaque(false); // Transparent background for top-right panel
                    topRightPanel.add(Box.createHorizontalGlue()); // Push components to the right
                    topRightPanel.add(imageLabel); // Add image to the panel
                    topRightPanel.add(copyButton); // Add copy button to the panel

                    // Create a JTextArea for the code
                    JTextArea codeTextArea = new JTextArea(parts[i]);
                    codeTextArea.setEditable(false);
                    codeTextArea.setLineWrap(true);
                    codeTextArea.setWrapStyleWord(true);
                    codeTextArea.setFont(new Font("Monospaced", Font.PLAIN, 12)); // Monospaced font for code
                    codeTextArea.setBackground(new Color(43, 43, 43)); // Dark background for code
                    codeTextArea.setForeground(Color.WHITE); // Light text color
                    codeTextArea.setBorder(BorderFactory.createEmptyBorder(5, 10, 5, 10)); // Inner padding

                    // Add the top-right panel inside the code block panel (top-right corner of the code block)
                    codePanel.add(topRightPanel, BorderLayout.NORTH); // Place the top-right panel in the north
                    codePanel.add(codeTextArea, BorderLayout.CENTER); // Add the code JTextArea in the center

                    // Add the code panel to the content panel
                    contentPanel.add(codePanel, BorderLayout.CENTER);
                }

                // Add the content panel (either regular text or code block) to the aiResponsePanel
                aiResponsePanel.add(contentPanel);
            }

            // Create a JScrollPane for the parent scroll
            JScrollPane parentScrollPane = new JScrollPane(aiResponsePanel);
            parentScrollPane.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_AS_NEEDED);
            parentScrollPane.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED);

            // Create a margin border for spacing
            Border marginBorder = BorderFactory.createEmptyBorder(10, 15, 10, 15);
            // Create a rounded border for styling
            Border roundedBorder = new RoundedBorder(15); // 15 is the radius of the rounded corners
            // Combine the borders (margin + rounded)
            Border combinedBorder = BorderFactory.createCompoundBorder(marginBorder, roundedBorder);

            // Apply the combined border to the parent JScrollPane
            parentScrollPane.setBorder(combinedBorder);

            // Add the parent scroll pane to the chat panel
            chatPanel.add(parentScrollPane);
            addStarRatingPanel(aiResponsePanel);
            chatPanel.revalidate();
            chatPanel.repaint();
            // Scroll the chat panel to the bottom
            JScrollPane scrollPane = getScrollPane(chatPanel);
            if (scrollPane != null) {
                // After revalidating and adding components, explicitly scroll to the bottom
                SwingUtilities.invokeLater(() -> {
                    JScrollBar verticalScrollBar = scrollPane.getVerticalScrollBar();
                    verticalScrollBar.setValue(verticalScrollBar.getMaximum()); // Scroll to the bottom
                });
            }
        }

        // Variable to track whether the feedback panel is visible
        private boolean isFeedbackPanelVisible = false;

        private void addStarRatingPanel(JPanel chatPanel) {
            // Create a panel for the star rating buttons
            JPanel ratingPanel = new JPanel();
            ratingPanel.setLayout(new FlowLayout(FlowLayout.LEFT, 5, 0));  // A small gap of 5px between stars

            // Create 5 star buttons (using icons)
            JButton[] starButtons = new JButton[5];
            for (int i = 0; i < 5; i++) {
                starButtons[i] = new JButton();
                // Set an empty star icon by default
                starButtons[i].setIcon(IconLoader.getIcon("/icons/empty_star.png"));  // Use your own empty star icon
                final int rating = i + 1;  // Rating is 1 to 5
                starButtons[i].setContentAreaFilled(false); // Remove the default button background
                starButtons[i].setBorderPainted(false); // Remove the button border
                starButtons[i].setFocusPainted(false); // Remove focus paint when clicked
                starButtons[i].setPreferredSize(new Dimension(20, 20));  // Adjust size to fit icons

                // Set tooltips for each star rating
                switch (rating) {
                    case 1:
                        starButtons[i].setToolTipText("Strongly Disagree");
                        break;
                    case 2:
                        starButtons[i].setToolTipText("Disagree");
                        break;
                    case 3:
                        starButtons[i].setToolTipText("Neither Agree nor Disagree");
                        break;
                    case 4:
                        starButtons[i].setToolTipText("Agree");
                        break;
                    case 5:
                        starButtons[i].setToolTipText("Strongly Agree");
                        break;
                }

                // Add action listener to handle rating selection
                starButtons[i].addActionListener(new ActionListener() {
                    @Override
                    public void actionPerformed(ActionEvent e) {
                        setRating(rating, starButtons);  // Set the rating based on clicked star
                    }
                });
                ratingPanel.add(starButtons[i]);
            }

            // Create the "Feedback" button
            JButton feedbackButton = new JButton("Feedback");
            feedbackButton.setFocusPainted(false);
            feedbackButton.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    toggleFeedbackPanel(chatPanel);  // Toggle the visibility of the feedback panel
                }
            });

            // Add the rating panel and feedback button to the chat panel
            JPanel ratingWithFeedbackPanel = new JPanel();
            ratingWithFeedbackPanel.setLayout(new BorderLayout());
            ratingWithFeedbackPanel.add(ratingPanel, BorderLayout.WEST);
            ratingWithFeedbackPanel.add(feedbackButton, BorderLayout.EAST);

            chatPanel.add(ratingWithFeedbackPanel, BorderLayout.NORTH);

            // Revalidate and repaint the chat panel
            chatPanel.revalidate();
            chatPanel.repaint();
        }

        private void setRating(int rating, JButton[] starButtons) {
            for (int i = 0; i < 5; i++) {
                if (i < rating) {
                    // Set filled star icon
                    starButtons[i].setIcon(IconLoader.getIcon("/icons/filled_star.png"));
                } else {
                    // Set empty star icon
                    starButtons[i].setIcon(IconLoader.getIcon("/icons/empty_star.png"));
                }
            }
        }

        private void toggleFeedbackPanel(JPanel chatPanel) {
            if (isFeedbackPanelVisible) {
                // If the feedback panel is visible, remove it
                chatPanel.remove(chatPanel.getComponentCount() - 1);  // Assuming it's the last component
                isFeedbackPanelVisible = false;
            } else {
                // If the feedback panel is not visible, show it
                showFeedbackPanel(chatPanel);
                isFeedbackPanelVisible = true;
            }

            // Revalidate and repaint the chat panel
            chatPanel.revalidate();
            chatPanel.repaint();
        }

        private void showFeedbackPanel(JPanel chatPanel) {
            // Create a panel to show feedback form
            JPanel feedbackPanel = new JPanel();
            feedbackPanel.setLayout(new BoxLayout(feedbackPanel, BoxLayout.Y_AXIS));

            // Create a JTextArea for feedback input
            JTextArea feedbackTextArea = new JTextArea(5, 30);  // 5 rows, 30 columns
            feedbackTextArea.setWrapStyleWord(true);
            feedbackTextArea.setLineWrap(true);
            feedbackTextArea.setBorder(BorderFactory.createLineBorder(Color.GRAY, 1));  // Border for text area
            JScrollPane feedbackScrollPane = new JScrollPane(feedbackTextArea);

            // Set a preferred size for the scroll pane to avoid overflow
            feedbackScrollPane.setPreferredSize(new Dimension(300, 100)); // Adjust based on desired width/height

            // Create a panel for buttons (Submit and Cancel)
            JPanel buttonPanel = new JPanel();
            JButton submitButton = new JButton("Submit");
            JButton cancelButton = new JButton("Cancel");

            // Handle submit action
            submitButton.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    // Handle the feedback submission logic here
                    String feedback = feedbackTextArea.getText();
                    if (!feedback.trim().isEmpty()) {
                        // Submit feedback (e.g., send to server or process)
                        JOptionPane.showMessageDialog(chatPanel, "Feedback submitted successfully!");
                        feedbackTextArea.setText("");  // Clear the textarea after submission
                    } else {
                        JOptionPane.showMessageDialog(chatPanel, "Please enter feedback before submitting.");
                    }
                }
            });

            // Handle cancel action
            cancelButton.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    // Clear feedback and hide the feedback panel
                    feedbackTextArea.setText("");
                    chatPanel.remove(feedbackPanel);  // Remove the feedback panel
                    isFeedbackPanelVisible = false;  // Mark the feedback panel as hidden
                    chatPanel.revalidate();  // Revalidate to update layout
                    chatPanel.repaint();     // Repaint to reflect changes
                }
            });

            // Add buttons to the button panel
            buttonPanel.add(submitButton);
            buttonPanel.add(cancelButton);

            // Create the "Your Feedback" label and center align it
            JLabel feedbackLabel = new JLabel("Your Feedback:");
            feedbackLabel.setAlignmentX(Component.CENTER_ALIGNMENT); // Center align the label

            // Add components to the feedback panel
            feedbackPanel.add(feedbackLabel); // Center-aligned label
            feedbackPanel.add(feedbackScrollPane);
            feedbackPanel.add(buttonPanel);

            // Set a preferred size for the feedback panel to prevent overflow
//            feedbackPanel.setPreferredSize(new Dimension(320, 150));  // Adjust based on the overall panel size

            // Add the feedback panel below the stars and feedback button
            chatPanel.add(feedbackPanel, BorderLayout.SOUTH);

            // Revalidate the layout to ensure it resizes properly and avoid scrollbars
            chatPanel.revalidate();  // Ensure proper layout resizing
            chatPanel.repaint();     // Ensure the UI is refreshed without scrollbars
        }




        @Override
        protected void process(List<String> chunks) {
            // Process the accumulated chunks and update the AI response in real-time
            for (String chunk : chunks) {
                // Update the UI with the chunk
                addAIResponse(chatPanel, chunk);
            }
        }

        @Override
        protected void done() {
            // Once done, hide the loading icon
            loadingIconLabel.setVisible(false);
            inputTextArea.setPreferredSize(new Dimension(250, 40));  // Reset the text area size
        }
    }
}
