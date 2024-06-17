import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import org.json.JSONObject;

public class CurrencyConverterApp extends JFrame {

    private static final String API_KEY = "de8f43dae37bca56e3a87336";
    private static final String BASE_URL = "https://v6.exchangerate-api.com/v6/" + API_KEY + "/latest/";

    private JTextField amountField;
    private JComboBox<String> fromCurrencyBox;
    private JComboBox<String> toCurrencyBox;
    private JLabel resultLabel;

    public CurrencyConverterApp() {
        setTitle("Currency Converter");
        setSize(400, 250);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null); 

        JPanel contentPane = new JPanel();
        contentPane.setBorder(new EmptyBorder(10, 10, 10, 10));
        setContentPane(contentPane);
        contentPane.setLayout(new BorderLayout(0, 10));

        JPanel inputPanel = new JPanel(new GridLayout(4, 2, 10, 10));
        contentPane.add(inputPanel, BorderLayout.CENTER);

        JLabel amountLabel = new JLabel("Amount:");
        amountLabel.setFont(amountLabel.getFont().deriveFont(Font.BOLD, 14f));
        amountField = new JTextField();
        amountField.setFont(amountField.getFont().deriveFont(Font.BOLD, 14f));
        inputPanel.add(amountLabel);
        inputPanel.add(amountField);

        JLabel fromCurrencyLabel = new JLabel("From Currency:");
        fromCurrencyLabel.setFont(fromCurrencyLabel.getFont().deriveFont(Font.BOLD, 14f));
        String[] currencies = {"USD", "EUR", "GBP", "INR"};
        fromCurrencyBox = new JComboBox<>(currencies);
        fromCurrencyBox.setFont(fromCurrencyBox.getFont().deriveFont(Font.BOLD, 14f));
        inputPanel.add(fromCurrencyLabel);
        inputPanel.add(fromCurrencyBox);

        JLabel toCurrencyLabel = new JLabel("To Currency:");
        toCurrencyLabel.setFont(toCurrencyLabel.getFont().deriveFont(Font.BOLD, 14f));
        toCurrencyBox = new JComboBox<>(currencies);
        toCurrencyBox.setFont(toCurrencyBox.getFont().deriveFont(Font.BOLD, 14f));
        inputPanel.add(toCurrencyLabel);
        inputPanel.add(toCurrencyBox);

        JButton convertButton = new JButton("Convert");
        convertButton.setFont(convertButton.getFont().deriveFont(Font.BOLD, 16f));
        convertButton.setBackground(new Color(0, 153, 51));
        convertButton.setForeground(Color.BLACK);
        inputPanel.add(convertButton);

        resultLabel = new JLabel("", SwingConstants.CENTER);
        resultLabel.setFont(resultLabel.getFont().deriveFont(Font.BOLD, 16f));
        contentPane.add(resultLabel, BorderLayout.SOUTH);

        convertButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                try {
                    double amount = Double.parseDouble(amountField.getText());
                    String fromCurrency = (String) fromCurrencyBox.getSelectedItem();
                    String toCurrency = (String) toCurrencyBox.getSelectedItem();
                    double convertedAmount = convertCurrency(fromCurrency, toCurrency, amount);
                    resultLabel.setText(String.format("%.2f %s is %.2f %s", amount, fromCurrency, convertedAmount, toCurrency));
                } catch (NumberFormatException ex) {
                    resultLabel.setText("Invalid amount!");
                } catch (IOException | InterruptedException ex) {
                    resultLabel.setText("Error fetching exchange rate data: " + ex.getMessage());
                }
            }
        });
    }

    public double convertCurrency(String fromCurrency, String toCurrency, double amount) throws IOException, InterruptedException {
        if (fromCurrency.equals(toCurrency)) {
            return amount; 
        }

        JSONObject rates = getExchangeRates(fromCurrency);
        if (rates == null) {
            throw new IOException("Error fetching rates");
        }

        double exchangeRate = rates.getDouble(toCurrency);

        return amount * exchangeRate;
    }

    private JSONObject getExchangeRates(String baseCurrency) throws IOException, InterruptedException {
        String url = BASE_URL + baseCurrency;
        HttpClient client = HttpClient.newHttpClient();
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(url))
                .build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

        if (response.statusCode() == 200) {
            JSONObject jsonResponse = new JSONObject(response.body());
            return jsonResponse.getJSONObject("conversion_rates");
        } else {
            throw new IOException("Error fetching exchange rates: " + response.statusCode());
        }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                try {
                    UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
                } catch (Exception e) {
                    e.printStackTrace();
                }
                new CurrencyConverterApp().setVisible(true);
            }
        });
    }
}
