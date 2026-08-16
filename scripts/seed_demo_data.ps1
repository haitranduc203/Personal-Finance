# FinTrack - Script nap du lieu giao dich mau qua ADB
param (
    [string]$AdbPath = "C:\Users\ADMIN\AppData\Local\Android\Sdk\platform-tools\adb.exe",
    [string]$MainActivity = "com.fintrack.app/.MainActivity"
)

Write-Host "====================================================" -ForegroundColor Cyan
Write-Host "  FinTrack - Tool Nap Du Lieu Giao Dich Mau Qua ADB" -ForegroundColor Cyan
Write-Host "====================================================" -ForegroundColor Cyan

if (-not (Test-Path $AdbPath)) {
    Write-Host "[!] Khong tim thay ADB tai: $AdbPath" -ForegroundColor Red
    exit 1
}

Write-Host "[*] Dang khoi chay ung dung FinTrack..." -ForegroundColor Yellow
& $AdbPath shell am start -n $MainActivity
Start-Sleep -Seconds 2

$sampleTransactions = @(
    @{ Amount = "45000"; Type = "EXPENSE"; CatX = 770; CatY = 1480; Note = "An trua pho bo" },
    @{ Amount = "55000"; Type = "EXPENSE"; CatX = 770; CatY = 1480; Note = "Ca phe Highland" },
    @{ Amount = "28000000"; Type = "INCOME"; CatX = 230; CatY = 1000; Note = "Luong thang 8" },
    @{ Amount = "1450000"; Type = "EXPENSE"; CatX = 770; CatY = 1350; Note = "Mua sam Uniqlo" },
    @{ Amount = "6500000"; Type = "EXPENSE"; CatX = 230; CatY = 1480; Note = "Tien nha" },
    @{ Amount = "8000000"; Type = "INCOME"; CatX = 770; CatY = 870; Note = "Thuong du an" }
)

Write-Host "[+] Bat dau nap $($sampleTransactions.Count) giao dich mau tu ben ngoai vao app..." -ForegroundColor Green

foreach ($tx in $sampleTransactions) {
    Write-Host "  -> Dang them: [$($tx.Type)] $($tx.Note) ($($tx.Amount) d)..." -ForegroundColor White
    
    # 1. Tap FAB (+) at bottom right
    & $AdbPath shell input tap 948 2028
    Start-Sleep -Milliseconds 700

    # 2. Chon Tab Neu la INCOME (786, 522)
    if ($tx.Type -eq "INCOME") {
        & $AdbPath shell input tap 786 522
        Start-Sleep -Milliseconds 400
    }

    # 3. Tap Amount field (540, 869)
    & $AdbPath shell input tap 540 869
    Start-Sleep -Milliseconds 300
    & $AdbPath shell input text $tx.Amount
    Start-Sleep -Milliseconds 300

    # 4. Chon Category
    & $AdbPath shell input tap $tx.CatX $tx.CatY
    Start-Sleep -Milliseconds 300

    # 5. Hide Keyboard
    & $AdbPath shell input keyevent 4
    Start-Sleep -Milliseconds 300

    # 6. Swipe down and click Luu giao dich
    & $AdbPath shell input swipe 540 2000 540 800 300
    Start-Sleep -Milliseconds 400
    & $AdbPath shell input tap 690 2214
    Start-Sleep -Milliseconds 800
}

Write-Host "[OK] Hoan tat nap du lieu mau thanh cong!" -ForegroundColor Green
Write-Host "====================================================" -ForegroundColor Cyan
