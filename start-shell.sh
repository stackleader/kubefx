rm -rf karaf/deploy/*
rm -rf karaf/local/*
mkdir karaf/deploy -p
cp com.stackleader.kubefx.feature/target/features/features.xml karaf/deploy/
karaf/bin/igb clean debug
