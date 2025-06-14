import javax.swing.*;
import java.awt.*;
import java.net.*;
import java.nio.charset.StandardCharsets;

public class ChatUDP extends JFrame {
    private JTextArea taMain;
    private JTextField tfMsg;
    private DatagramSocket sendSocket; //Сокет для отпраавки (автовыбор порта)
    private int receiverPort; //Порт, на котором слушает получатель

    private final String FRM_TITLE = "Our tiny chat";
    private final int FRM_LOC_X = 100;
    private final int FRM_LOC_Y = 100;
    private final int FRM_WIDTH = 400; //Начальная ширина
    private final int FRM_HEIGHT = 400; //Начальная высота
    private final int MIN_WIDTH = 300; //Минимальная ширина
    private final int MIN_HEIGHT = 300; //Минимальная высота

    private final String IP_BROADCAST = "255.255.255.255";

    private class thdReceiver extends Thread {
        @Override
        public void run() {
            try {
                customize();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        private void customize() throws Exception {
            //Создаём сокет на свободном порту (0 = любой свободный)
            DatagramSocket receiverSocket = new DatagramSocket(0);
            receiverPort = receiverSocket.getLocalPort(); //Запоминаем порт
            taMain.append("Listening on port: " + receiverPort + "\n");

            while (true) {
                byte[] receiveData = new byte[1024];
                DatagramPacket receivePacket = new DatagramPacket(receiveData, receiveData.length);
                receiverSocket.receive(receivePacket);
                String sentence = new String(receivePacket.getData(), 0, receivePacket.getLength(), StandardCharsets.UTF_8);

                //Добавляем сообщение в чат
                taMain.append(receivePacket.getAddress() + ":" + receivePacket.getPort() + ": " + sentence + "\n");

                //Автоматическая прокрутка вниз
                SwingUtilities.invokeLater(() -> {
                    taMain.setCaretPosition(taMain.getDocument().getLength());
                });
            }
        }
    }

    //Обработчик кнопки "Отправить"
    private void btnSend_Handler() throws Exception {
        String sentence = tfMsg.getText().trim();
        if (sentence.isEmpty()) return;

        tfMsg.setText("");
        byte[] sendData = sentence.getBytes(StandardCharsets.UTF_8);

        //Отправляем на широковещательный адрес и выбранный порт получателя
        DatagramPacket sendPacket = new DatagramPacket(
                sendData,
                sendData.length,
                InetAddress.getByName(IP_BROADCAST),
                receiverPort
        );
        sendSocket.send(sendPacket);
    }

    //Настройка GUI с поддержкой изменения размера
    private void frameDraw() {
        setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        setTitle(FRM_TITLE);
        setLocation(FRM_LOC_X, FRM_LOC_Y);
        setSize(FRM_WIDTH, FRM_HEIGHT);
        setMinimumSize(new Dimension(MIN_WIDTH, MIN_HEIGHT)); //Минимальный размер окна
        setResizable(true);//Разрешение на изменение размеров окна

        //Основная область чата (с прокруткой)
        taMain = new JTextArea();
        taMain.setDocument(new LimitedLinesDocument(1000)); //Ограничение в 1000 строк
        taMain.setEditable(false);
        JScrollPane spMain = new JScrollPane(taMain); //Скролл, прокрутка
        spMain.setPreferredSize(new Dimension(FRM_WIDTH, FRM_HEIGHT - 50));

        //Поле ввода сообщения
        tfMsg = new JTextField();

        //Кнопка отправки
        JButton btnSend = new JButton("Отправить");
        btnSend.addActionListener(e -> {
            try {
                btnSend_Handler();
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        });

        //Панель для нижней части (поле ввода + кнопка)
        JPanel bottomPanel = new JPanel(new BorderLayout());
        bottomPanel.add(tfMsg, BorderLayout.CENTER);
        bottomPanel.add(btnSend, BorderLayout.EAST);

        //Основной layout
        setLayout(new BorderLayout());
        add(spMain, BorderLayout.CENTER);
        add(bottomPanel, BorderLayout.SOUTH);
    }

    //Инициализация сокетов и запуск потока
    private void antiStatic() throws SocketException {
        sendSocket = new DatagramSocket(); //Автоматически выбирает свободный порт
        frameDraw();
        new thdReceiver().start();
        setVisible(true);
    }

    public static void main(String[] args) {
        try {
            new ChatUDP().antiStatic();
        } catch (Exception exc) {
            exc.printStackTrace();
        }

    }
}