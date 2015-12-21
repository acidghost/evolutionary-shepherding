import pandas as pd
import numpy as np
import matplotlib.pyplot as plt
from scipy.stats import ttest_ind


# stats_path = "/Users/erotundo/git/UniNotes/ASO/statistics"
stats_path = "../statistics"

scenarios = ['hetero.1v1',
             'hetero.2v1',
             'hetero.3v1',
             'hetero.3v2.ho',
             'hetero.3v3.ho']

# int generation;
# List<SubpopData> subpopData;
# int corralled;
# int escaped;
# int evaluations;
# double corralledRatio;
# double escapedRatio;
# double popMeanFitness;
# double popBestOfGeneration;
# double popBestSoFar;

# double mean;
# double bestOfGeneration;
# double bestSoFar;

fitness_cols = {
    1: [2],
    2: [2, 5],
    3: [2, 5, 8]
}

dfs = {}
for scenario in scenarios:
    num_sub_pop = int(scenario.split('.')[1].split('v')[0])
    print scenario + "\n"
    df = pd.DataFrame()
    for run in range(1, 41):
        file_path = stats_path + str("/" + str(scenario) + "/" + str(run)+'.stat')     
        df_run = pd.read_csv(file_path, sep=' ', header=None, index_col=0, skipfooter=1, engine='python')
        dogs_fitness = df_run.iloc[:, fitness_cols[num_sub_pop]].mean(axis=1)
        mean_dog_fitness = dogs_fitness.mean()
        # print mean_dog_fitness
        mean_corralled_ratio = df_run.iloc[:, -6].mean()
        # print mean_corralled_ratio
        mean_escaped_ratio = df_run.iloc[:, -5].mean()
        # print mean_escaped_ratio
        
        df.loc[run, "fitness"] = mean_dog_fitness
        df.loc[run, "corralled_ratio"] = mean_corralled_ratio
        df.loc[run, "escaped_ratio"] = mean_escaped_ratio

    dfs[scenario] = df
    # print df
    print str(df.loc[:, "fitness"].describe()) + "\n"
    print str(df.loc[:, "corralled_ratio"].describe()) + "\n"
    print str(df.loc[:, "escaped_ratio"].describe()) + "\n\n\n"

## T-TEST
hetero1v1 = dfs["hetero.1v1"]
hetero3v3ho = dfs["hetero.3v3.ho"]
print "Fitness T-test 1vs1 - 3vs3"
print "%.6f\n" % ttest_ind(hetero1v1.loc[:, "fitness"].values, hetero3v3ho.loc[:, "fitness"].values)[1]
print "Corralled ratio T-test 1vs1 - 3vs3"
print "%.6f\n" % ttest_ind(hetero1v1.loc[:, "corralled_ratio"].values, hetero3v3ho.loc[:, "corralled_ratio"].values)[1]

hetero3v1 = dfs["hetero.3v1"]
print "Fitness T-test 1vs1 - 3vs1"
print "%.6f\n" % ttest_ind(hetero1v1.loc[:, "fitness"].values, hetero3v1.loc[:, "fitness"].values)[1]
print "Corralled ratio T-test 1vs1 - 3vs1"
print "%.6f\n" % ttest_ind(hetero1v1.loc[:, "corralled_ratio"].values, hetero3v1.loc[:, "corralled_ratio"].values)[1]

print "Fitness T-test 3vs1 - 3vs3"
print "%.6f\n" % ttest_ind(hetero3v1.loc[:, "fitness"].values, hetero3v3ho.loc[:, "fitness"].values)[1]
print "Corralled ratio T-test 3vs1 - 3vs3"
print "%.6f\n" % ttest_ind(hetero3v1.loc[:, "corralled_ratio"].values, hetero3v3ho.loc[:, "corralled_ratio"].values)[1]

hetero3v2ho = dfs["hetero.3v2.ho"]
print "Fitness T-test 3vs2 - 3vs3"
print "%.6f\n" % ttest_ind(hetero3v2ho.loc[:, "fitness"].values, hetero3v3ho.loc[:, "fitness"].values)[1]
print "Corralled ratio T-test 3vs2 - 3vs3"
print "%.6f\n" % ttest_ind(hetero3v2ho.loc[:, "corralled_ratio"].values, hetero3v3ho.loc[:, "corralled_ratio"].values)[1]


## BAR CHARTS
data1 = [hetero1v1, dfs["hetero.2v1"], hetero3v1]
data2 = [hetero3v1, hetero3v2ho, hetero3v3ho]
data = [data1, data2]

ind = np.arange(2)
colors = ['r', 'b', 'g']
width = .15       # the width of the bars
left_space = .5
for j, d in enumerate(data):
    fig, ax = plt.subplots()
    rects = []
    for i, df in enumerate(d):
        corralled = df.loc[:, "corralled_ratio"]
        escaped = df.loc[:, "escaped_ratio"]
        rects1 = ax.bar(left_space + ind + (i * width), [corralled.mean(), escaped.mean()], width, color=colors[i], yerr=[corralled.std(), escaped.std()])
        rects.append(rects1)

    # add some text for labels, title and axes ticks
    ax.set_ylabel('Ratio')
    ax.set_xticks(left_space + ind + (width * 1.5))
    ax.set_xticklabels(("Corralled", "Escaped"))

    if j==0:
        ax.set_title('Hetero vs. 1 sheep - 40 runs')
        ax.legend((rects[0], rects[1], rects[2]), ('1 vs. 1', '2 vs. 1', '3 vs. 1'))
    else:
        ax.set_title('Hetero vs. more sheep - 40 runs')
        ax.legend((rects[0], rects[1], rects[2]), ('3 vs. 1', '3 vs. 2', '3 vs. 3'))

    plt.ylim([0, 1])
    plt.show()

