import javax.swing.text.AttributeSet;
import javax.swing.text.BadLocationException;
import javax.swing.text.PlainDocument;

public class LimitedLinesDocument extends PlainDocument {
    private final int maxLines; //Максимальное количество строк

    public LimitedLinesDocument(int maxLines) {
        this.maxLines = maxLines;
    }

    @Override
    public void insertString(int offset, String str, AttributeSet attr) throws BadLocationException {
        if (str == null) {
            return;
        }

        String currentText = getText(0, getLength());
        String newText = currentText.substring(0, offset) + str + currentText.substring(offset);

        //Разделяем текст на строки
        String[] lines = newText.split("\n", -1); //"-1" сохраняет пустые строки

        //Если строк стало больше максимума - удаляем лишнее
        if (lines.length > maxLines) {
            //Находим, сколько строк нужно удалить
            int linesToRemove = lines.length - maxLines;
            int lengthToRemove = 0;

            //Считаем длину первых 'linesToRemove' строк (+ символы перевода строки)
            for (int i = 0; i < linesToRemove; i++) {
                lengthToRemove += lines[i].length() + 1; //"+1" учитывает '\n'
            }

            //Удаляем лишние строки из начала
            super.remove(0, lengthToRemove);
        }

        //Вставляем новый текст
        super.insertString(offset, str, attr);
    }
}
