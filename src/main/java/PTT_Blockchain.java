import java.security.Security;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Scanner;


public class PTT_Blockchain {
    public static ArrayList<VNPT_Tai> blockchain = new ArrayList<VNPT_Tai>();
    public static HashMap<String,TransactionOutput> UTXOs = new HashMap<String,TransactionOutput>();
    public static int difficulty = 5; // độ khó 5
    public static float minimumTransaction = 1f; // số lượng thấp nhất cho chuyển
    public static Store store1; //Kho 1
    public static Store store2; //Kho 2
    public static Transaction genesisTransaction;

    public static void main(String[] args){
//add our blocks to the blockchain ArrayList:
        Security.addProvider(new org.bouncycastle.jce.provider.BouncyCastleProvider()); //Thiết lập bảo mật bằng phương thức BouncyCastleProvider
// Khởi tạo các kho:
        store1 = new Store();
        store2 = new Store();
        Store storeese = new Store();

        Scanner scan = new Scanner(System.in);// phương thức nhập từ bàn phím

// Nhập số lượng tồn kho cho kho 1
        System.out.print("Nhập số lượng điện thoại trong kho 1: ");
        int Inventory1 = scan.nextInt();

//Khởi tạo số lượng tồn kho gốc cho kho 1
        genesisTransaction = new Transaction(storeese.publicKey, store1.publicKey, Inventory1, null);
        genesisTransaction.generateSignature(storeese.privateKey);	 //Gán thủ công khóa bí mật vào giao dịch gốc
        genesisTransaction.transactionId = "0"; //Gán ID đầu tiên cho khởi tạo đầu tiên
        genesisTransaction.outputs.add(new TransactionOutput(genesisTransaction.reciepient, genesisTransaction.value, genesisTransaction.transactionId)); //Thêm Transactions Output
        UTXOs.put(genesisTransaction.outputs.get(0).id, genesisTransaction.outputs.get(0)); //Lưu khởi tạo đầu tiên vào danh sách UTXOs.
//Tạo khối kho 1
        System.out.println("Đang tạo tồn Kho 1 .... ");
        VNPT_Tai genesis = new VNPT_Tai("0");
        genesis.addTransaction(genesisTransaction);
        addBlock(genesis);

// Nhập số lượng tồn kho cho kho 2
        System.out.print("Nhập số lượng điện thoại trong kho 2: ");
        int Inventory2 = scan.nextInt();

//Khởi tạo số lượng tồn kho gốc cho kho 2
        Transaction transaction2 = new Transaction(storeese.publicKey, store2.publicKey, Inventory2, null);
        transaction2.generateSignature(storeese.privateKey);    //Gán thủ công khóa bí mật vào giao dịch gốc
        transaction2.transactionId = "0"; //Gán ID cho giao dịch gốc
        transaction2.outputs.add(new TransactionOutput(transaction2.reciepient, transaction2.value, transaction2.transactionId)); //Thêm Transactions Output
        UTXOs.put(transaction2.outputs.get(0).id, transaction2.outputs.get(0)); //Lưu giao dịch đầu tiên vào danh sách UTXOs.
//Tạo khối kho 2
        System.out.println("Đang tạo tồn Kho 2 .... ");
        VNPT_Tai block1 = new VNPT_Tai(genesis.hash);
        genesis.addTransaction(genesisTransaction);
        addBlock(block1);

// Thông tin số lượng điện thoại đang có trong kho:
        System.out.println("Số điện thoại đang có trong kho 1 là: " + store1.getBalance());
        System.out.println("Số điện thoại đang có trong kho 2 là: " + store2.getBalance());

// tạo giao dịch chuyển điện thoại từ kho 1 đến kho 2
        System.out.print("Nhập số lượng điện thoại cần chuyển từ kho 1 sang kho 2: ");
        int transfer = scan.nextInt();

        VNPT_Tai block2 = new VNPT_Tai(block1.hash);
        System.out.println("Đang thực hiện chuyển " + transfer +" điện thoại từ kho 1 sang kho 2.........");
        block2.addTransaction(store1.sendFunds(store2.publicKey, transfer ));
        addBlock(block2);
        System.out.println("Số ĐT mới của kho 1 là : " + store1.getBalance());
        System.out.println("Số ĐT mới của kho 2 là : " + store2.getBalance());

        isChainValid();
    }

//Phương thức kiểm tra tính toàn vẹn của Blockchain
    public static Boolean isChainValid() {
        VNPT_Tai currentBlock;
        VNPT_Tai previousBlock;
        String hashTarget = new String(new char[difficulty]).replace('\0', '0');
        HashMap<String,TransactionOutput> tempUTXOs = new HashMap<String,TransactionOutput>(); //Tạo một danh sách hoạt động tạm thời của các giao dịch chưa được thực thi tại một trạng thái khối nhất định.
        tempUTXOs.put(genesisTransaction.outputs.get(0).id, genesisTransaction.outputs.get(0));

        //loop through blockchain to check hashes:
        for(int i=1; i < blockchain.size(); i++) {

            currentBlock = blockchain.get(i);
            previousBlock = blockchain.get(i-1);
            //Kiểm tra, so sánh mã băm đã đăng ký với mã băm được tính toán
            if(!currentBlock.hash.equals(currentBlock.calculateHash()) ){
                System.out.println("#Mã băm khối hiện tại không khớp");
                return false;
            }
            //So sánh mã băm của khối trước với mã băm của khối trước đã được đăng ký
            if(!previousBlock.hash.equals(currentBlock.previousHash) ) {
                System.out.println("#Mã băm khối trước không khớp");
                return false;
            }
            //Kiểm tra xem mã băm có lỗi không
            if(!currentBlock.hash.substring( 0, difficulty).equals(hashTarget)) {
                System.out.println("#Khối này không đào được do lỗi!");
                return false;
            }

            //Vòng lặp kiểm tra các giao dịch
            TransactionOutput tempOutput;
            for(int t=0; t <currentBlock.transactions.size(); t++) {
                Transaction currentTransaction = currentBlock.transactions.get(t);

                if(!currentTransaction.verifySignature()) {
                    System.out.println("#Chữ ký số của giao dịch (" + t + ") không hợp lệ");
                    return false;
                }
                if(currentTransaction.getInputsValue() != currentTransaction.getOutputsValue()) {
                    System.out.println("#Các đầu vào không khớp với đầu ra trong giao dịch (" + t + ")");
                    return false;
                }

                for(TransactionInput input: currentTransaction.inputs) {
                    tempOutput = tempUTXOs.get(input.transactionOutputId);

                    if(tempOutput == null) {
                        System.out.println("#Các đầu vào tham chiếu trong giao dịch (" + t + ") bị thiếu!");
                        return false;
                    }

                    if(input.UTXO.value != tempOutput.value) {
                        System.out.println("#Các đầu vào tham chiếu trong giao dịch (" + t + ") có giá trị không hợp lệ");
                        return false;
                    }

                    tempUTXOs.remove(input.transactionOutputId);
                }

                for(TransactionOutput output: currentTransaction.outputs) {
                    tempUTXOs.put(output.id, output);
                }

                if( currentTransaction.outputs.get(0).reciepient != currentTransaction.reciepient) {
                    System.out.println("#Giao dịch(" + t + ") có người nhận không đúng!");
                    return false;
                }
                if( currentTransaction.outputs.get(1).reciepient != currentTransaction.sender) {
                    System.out.println("#Đầu ra của giao (" + t + ") không đúng với người gửi.");
                    return false;
                }

            }

        }
        System.out.println("Chuỗi khối hợp lệ!");
        return true;
    }
    public static void addBlock(VNPT_Tai newBlock) {
        newBlock.mineBlock(difficulty);
        blockchain.add(newBlock);
    }

}
