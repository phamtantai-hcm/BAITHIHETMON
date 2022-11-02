import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.SecureRandom;
import java.security.spec.ECGenParameterSpec;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Scanner;

public class Store {
    public static ArrayList<VNPT_Tai> blockchain = new ArrayList<VNPT_Tai>();
    public static Transaction transactionstore;
    public static int difficulty = 0;
    public PrivateKey privateKey;
    public PublicKey publicKey;

    public HashMap<String,TransactionOutput> UTXOs = new HashMap<String,TransactionOutput>();

    public Store() {
        generateKeyPair();
    }

    public void generateKeyPair() {
        try {
            KeyPairGenerator keyGen = KeyPairGenerator.getInstance("ECDSA","BC");
            //Elliptic Curve Digital Signature Algorithm - thuật toán sinh chữ ký số dựa trên đường cong Elliptic
            SecureRandom random = SecureRandom.getInstance("SHA1PRNG"); //Tạo ra mã ngẫu nhiên
            //Tạo một đặc tả tham số để tạo các tham số miền đường cong Elliptic
            ECGenParameterSpec ecSpec = new ECGenParameterSpec("prime192v1");

            // Khởi tạo bộ tạo khóa và sinh một KeyPair
            keyGen.initialize(ecSpec, random); //256
            KeyPair keyPair = keyGen.generateKeyPair();
            // Thiết lập khóa bảo mật và khóa công khai cho Cặp khóa
            privateKey = keyPair.getPrivate();
            publicKey = keyPair.getPublic();

        }catch(Exception e) {
            throw new RuntimeException(e);
        }
    }

    public float getBalance() {
        float total = 0;
        for (Map.Entry<String, TransactionOutput> item : PTT_Blockchain.UTXOs.entrySet()){
            TransactionOutput UTXO = item.getValue();
            if(UTXO.isMine(publicKey)) {
                UTXOs.put(UTXO.id,UTXO);
                total += UTXO.value ;
            }
        }
        return total;
    }
//Kiểm tra lớn hơn số nhập vào
    public Transaction sendFunds(PublicKey _recipient,float value ) {
        if(getBalance() < value) {

            while (getBalance() < value) {
                System.out.println("Giao dịch lỗi");
                System.out.println("Số điện thoại yêu cầu chuyển cao hơn với số tồn Kho 1");
                VNPT_Tai genesis = new VNPT_Tai("0");
                genesis.addTransaction(transactionstore);
                addBlock(genesis);
                Scanner scanner = new Scanner(System.in);
                System.out.print("Lưu ý: ------ SỐ LƯỢNG ĐIỆN THOẠI YÊU CẦU PHẢI <= TỒN KHO 1 ------\n");
                System.out.print("Nhập lại số lượng điện thoại cần chuyển từ kho 1 sang kho 2: ");
                value = scanner.nextInt();
            }

        }
        ArrayList<TransactionInput> inputs = new ArrayList<TransactionInput>();

        float total = 0;
        for (Map.Entry<String, TransactionOutput> item: UTXOs.entrySet()){
            TransactionOutput UTXO = item.getValue();
            total += UTXO.value;
            inputs.add(new TransactionInput(UTXO.id));
            if(total > value) break;
        }

        Transaction newTransaction = new Transaction(publicKey, _recipient , value, inputs);
        newTransaction.generateSignature(privateKey);

        for(TransactionInput input: inputs){
            UTXOs.remove(input.transactionOutputId);
        }

        return newTransaction;
    }
// add block
    public static void addBlock(VNPT_Tai newBlock) {
        newBlock.mineBlock(difficulty);
        blockchain.add(newBlock);
    }
}
