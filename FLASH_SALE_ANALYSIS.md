# Bao cao flash sale

## 1. Phan tich bai toan (I/O)

### Input
- `productId`: ma san pham dang flash sale.
- `customerName`: ten khach dat hang.
- `quantity`: so luong muon mua, voi bai toan nay thuong la `1`.

### Output
- Thanh cong:
  - Tao 1 don hang moi.
  - Giam ton kho ngay trong transaction.
  - Tra ve `orderId`, thong diep `Dat hang thanh cong`, `remainingStock`.
- That bai:
  - Neu stock khong du: tra ve `SOLD_OUT` va thong diep `Het hang`.
  - Neu khong tim thay san pham: tra ve `NOT_FOUND`.
  - Neu request thieu du lieu: tra ve `INVALID_REQUEST`.

### Rui ro neu khong khoa du lieu
- Kho con `1`.
- Thread A doc stock = `1`.
- Thread B cung doc stock = `1`.
- Ca hai cung tru kho va tao don.
- Ket qua la co 2 don thanh cong trong khi kho chi con 1, day la hien tuong over-selling.

## 2. Giai phap de xuat

### Lua chon chinh
- Su dung `PESSIMISTIC_WRITE` khi doc `Product`.
- Giu `@Version` tren entity `Product` nhu lop bao ve bo sung.
- Chay transaction voi `Isolation.READ_COMMITTED`.
- Khong dung `exception` trong nghiep vu; service tu kiem tra dieu kien va tra ve `result status`.

### Ly do
- Flash sale la tinh huong ghi dong thoi rat cao, so luong ton kho lai rat nho.
- `PESSIMISTIC_WRITE` khoa dong san pham ngay khi bat dau xu ly mua hang, buoc request den sau phai cho request truoc commit/rollback.
- Khi request thu hai duoc phep doc lai, no se nhin thay stock moi nhat. Neu stock = `0` thi tra `Het hang`.
- `@Version` van co gia tri bao ve neu ve sau service duoc mo rong sang luong cap nhat khong dung row lock.

## 3. Thiet ke luong xu ly

```text
Client nhan "Mua ngay"
    |
    v
Bat dau transaction (READ_COMMITTED)
    |
    v
SELECT product ... FOR UPDATE
    |
    +--> Khong tim thay san pham -> tra `NOT_FOUND`
    |
    v
Kiem tra stock >= quantity ?
    |
    +--> Khong du -> tra `SOLD_OUT`
    |
    v
Tru stock trong entity Product
    |
    v
Tao FlashSaleOrder
    |
    v
flush() de dong bo du lieu xuong DB
    |
    v
Commit transaction
    |
    v
Tra ve "Dat hang thanh cong"
```

## 4. Du lieu dau ra mong doi khi 10 nguoi tranh 5 san pham
- Toi da 5 don hang duoc tao.
- Final stock = 0.
- Cac request con lai nhan `SOLD_OUT`.
- Khong co truong hop tao hon 5 don.

## 5. Tep code chinh
- `FlashSaleService`: xu ly transaction, khoa dong, validate request, tru kho va tao don.
- `ProductRepository`: `findByIdForUpdate()` voi `PESSIMISTIC_WRITE`.
- `Bai3Ss14ApplicationTests`: mo phong 10 luong mua dong thoi de xac nhan khong ban lo.
