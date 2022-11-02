import java.util.ArrayList;
import java.util.Date;

public class VNPT_Tai {
    public String hash;
    public String previousHash;

    public String merkleRoot; // Lưu trữ các giao dịch

    public ArrayList<Transaction> transactions = new ArrayList<Transaction>(); //Lưu trữ các transactions
    public long timeStamp;
    public int nonce;

 // Phương thức tạo khối
    public VNPT_Tai( String previousHash) {
        this.previousHash = previousHash;
        this.timeStamp = (new Date()).getTime();
        this.hash = this.calculateHash();
    }
 // Phương thức tính mã băm Hash
    public String calculateHash() {
        String calculatedhash = StringUtil.applySha256(
             previousHash +
                     Long.toString(timeStamp) +
                     Integer.toString(nonce) +
                     merkleRoot
        );
        return calculatedhash;
    }
//Phương thức đào khối mineBlock bằng thuật toán đồng thuận proof-of-work
    public void mineBlock(int difficulty) {
        merkleRoot = StringUtil.getMerkleRoot(transactions);
        String target = StringUtil.getDificultyString(difficulty); //Khởi tạo chuỗi với độ khó  là "0" do cấu hình máy số càng lớn tg đào càn lâu
        while(!hash.substring( 0, difficulty).equals(target)) {
            nonce ++;
            hash = calculateHash();
        }
        System.out.println("Block Mined!!! : " + hash);
    }

//Thêm Transaction vào Block
    public boolean addTransaction(Transaction transaction) {
//xử lý giao dịch và kiểm tra xem có hợp lệ không; Nếu là khối là khối gốc thì không cần kiểm tra.
        if(transaction == null) return false;
        if((!"0".equals(previousHash))) {
            if((transaction.processTransaction() != true)) {
                System.out.println("Giao dịch không xử lý được!.");
                return false;
            }
        }

        transactions.add(transaction);
        System.out.println("Giao dịch đã được thêm thành công vào khối.");
        return true;
    }
}
