// Menampilkan hanya kategori yang sesuai dengan jenis transaksi yang dipilih.
document.addEventListener('DOMContentLoaded', function () {
    var jenisSelect = document.getElementById('jenis');
    var kategoriSelect = document.getElementById('kategori_id');
    var hint = document.getElementById('kategori-hint');
    if (!jenisSelect || !kategoriSelect) return;

    function filterKategori() {
        var jenis = jenisSelect.value;
        var previous = kategoriSelect.value;
        var previousOk = false;
        Array.prototype.forEach.call(kategoriSelect.options, function (opt) {
            if (!opt.value) { opt.hidden = false; opt.disabled = false; return; }
            var cocok = !jenis || opt.dataset.jenis === jenis;
            opt.hidden = !cocok;
            opt.disabled = !cocok;
            if (cocok && opt.value === previous) previousOk = true;
        });
        if (!previousOk) kategoriSelect.value = '';
        if (hint) {
            hint.textContent = !jenis
                ? 'Pilih jenis transaksi terlebih dahulu.'
                : 'Kategori ditampilkan berdasarkan jenis transaksi.';
        }
    }
    jenisSelect.addEventListener('change', filterKategori);
    filterKategori();
});
