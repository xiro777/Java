for /R %%F in (BarcodeReader*.jar) do (
start /B javaw -Xmx500m -jar "%%F"
)